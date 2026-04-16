# ==============================================================================
#  Dockerfile — Multi-stage build para o backend RideFlow
#
#  Stage 1 (builder): compila e empacota o JAR com Maven
#  Stage 2 (runtime): imagem mínima com JRE 21, sem fontes nem Maven
#
#  Build:
#    docker build -t rideflow-backend .
#
#  Otimizações:
#    - Dependências Maven em camada separada (cache Docker entre builds)
#    - Usuário não-root (appuser) por segurança
#    - JVM flags otimizadas para containers
# ==============================================================================

# ── Stage 1: Builder ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# 1. Copia somente os descritores de dependência primeiro.
#    Essa camada é cacheada enquanto pom.xml e mvnw não mudarem —
#    evita re-download de todo o repositório Maven a cada build.
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd ./

# Garante permissão de execução no wrapper
RUN chmod +x mvnw

# 2. Baixa todas as dependências (sem compilar) — camada cacheável
RUN ./mvnw dependency:go-offline -q

# 3. Copia o código-fonte e empacota, pulando os testes
#    (testes rodam em CI, não no build da imagem de produção)
COPY src/ src/

RUN ./mvnw package -DskipTests -q


# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Metadados OCI
LABEL org.opencontainers.image.title="RideFlow Backend" \
      org.opencontainers.image.description="Sistema de corridas em tempo real — Java 21 + Spring Boot 3" \
      org.opencontainers.image.version="1.0.0"

# Usuário não-root: princípio de menor privilégio
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copia apenas o JAR do stage anterior
COPY --from=builder /build/target/rideflow-backend-*.jar app.jar

# Ajusta dono do arquivo
RUN chown appuser:appgroup app.jar

USER appuser

# Porta declarativa (documentation only — expose não publica)
EXPOSE 8080

# ── JVM flags para execução em container ──────────────────────────────────────
#
#  -XX:+UseContainerSupport          reconhece CPU/memória do cgroup
#  -XX:MaxRAMPercentage=75.0         usa 75% da RAM do container
#  -XX:+OptimizeStringConcat         otimização de string (Java 21)
#  -Djava.security.egd               acelera geração de números aleatórios em Linux
#  -Dspring.profiles.active          sobrescrito via variável de ambiente
#  --enable-preview                  permite features preview do Java 21 se necessário
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+OptimizeStringConcat", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]
