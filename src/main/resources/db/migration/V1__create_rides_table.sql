-- ── Tipo enum para status da corrida ─────────────────────────────────────────
CREATE TYPE ride_status AS ENUM (
    'PENDING',    -- criada, aguardando motorista
    'ACCEPTED',   -- motorista aceitou
    'REJECTED',   -- motorista recusou (volta para PENDING para nova tentativa)
    'CANCELLED',  -- passageiro cancelou
    'COMPLETED'   -- corrida finalizada
);

-- ── Tabela principal de corridas ──────────────────────────────────────────────
CREATE TABLE rides (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),

    -- Identificadores de negócio
    user_id         UUID            NOT NULL,
    driver_id       UUID,                              -- NULL até ser aceita

    -- Estado
    status          ride_status     NOT NULL DEFAULT 'PENDING',

    -- Endereço de origem (flattened — sem JOIN para leitura rápida)
    origin_cep          VARCHAR(9)      NOT NULL,      -- formato: 00000-000
    origin_logradouro   VARCHAR(255)    NOT NULL,
    origin_numero       VARCHAR(20)     NOT NULL,
    origin_complemento  VARCHAR(100),                  -- opcional
    origin_bairro       VARCHAR(100)    NOT NULL,
    origin_cidade       VARCHAR(100)    NOT NULL,
    origin_estado       CHAR(2)         NOT NULL,      -- sigla UF: SP, RJ, ...

    -- Endereço de destino
    dest_cep            VARCHAR(9)      NOT NULL,
    dest_logradouro     VARCHAR(255)    NOT NULL,
    dest_numero         VARCHAR(20)     NOT NULL,
    dest_complemento    VARCHAR(100),
    dest_bairro         VARCHAR(100)    NOT NULL,
    dest_cidade         VARCHAR(100)    NOT NULL,
    dest_estado         CHAR(2)         NOT NULL,

    -- Auditoria
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    accepted_at     TIMESTAMPTZ,                       -- NULL até status = ACCEPTED

    CONSTRAINT pk_rides PRIMARY KEY (id),

    -- Garante que driver_id é obrigatório quando a corrida foi aceita ou concluída
    CONSTRAINT chk_driver_on_accepted
        CHECK (
            status NOT IN ('ACCEPTED', 'COMPLETED') OR driver_id IS NOT NULL
        )
);

-- ── Trigger: atualiza updated_at automaticamente ──────────────────────────────
CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_rides_updated_at
    BEFORE UPDATE ON rides
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

-- ── Índices ───────────────────────────────────────────────────────────────────

-- Consultas por passageiro (tela "Minhas corridas")
CREATE INDEX idx_rides_user_id
    ON rides (user_id);

-- Consultas por motorista (corridas atribuídas)
CREATE INDEX idx_rides_driver_id
    ON rides (driver_id)
    WHERE driver_id IS NOT NULL;           -- índice parcial: ignora linhas NULL

-- Listagem por status (fila de corridas PENDING)
CREATE INDEX idx_rides_status
    ON rides (status);

-- Listagem paginada por data de criação (padrão desc)
CREATE INDEX idx_rides_created_at_desc
    ON rides (created_at DESC);

-- Consulta composta: corridas PENDING ordenadas por data (caso de uso crítico)
CREATE INDEX idx_rides_pending_created_at
    ON rides (created_at DESC)
    WHERE status = 'PENDING';
