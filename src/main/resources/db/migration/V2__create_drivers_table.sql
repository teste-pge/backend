-- ── Tipo enum para status do motorista ────────────────────────────────────────
CREATE TYPE driver_status AS ENUM (
    'AVAILABLE',
    'BUSY'
);

-- ── Tabela de motoristas ──────────────────────────────────────────────────────
CREATE TABLE drivers (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    name            VARCHAR(255)    NOT NULL,
    vehicle_plate   VARCHAR(10)     NOT NULL,
    status          driver_status   NOT NULL DEFAULT 'AVAILABLE',

    CONSTRAINT pk_drivers PRIMARY KEY (id)
);

-- ── Seed: 5 motoristas mockados (não há tela de cadastro) ─────────────────────
INSERT INTO drivers (id, name, vehicle_plate, status) VALUES
    ('d1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1', 'Carlos Silva',   'ABC-1234', 'AVAILABLE'),
    ('d2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2', 'Ana Santos',     'DEF-5678', 'AVAILABLE'),
    ('d3d3d3d3-d3d3-d3d3-d3d3-d3d3d3d3d3d3', 'Roberto Lima',   'GHI-9012', 'AVAILABLE'),
    ('d4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4', 'Maria Oliveira', 'JKL-3456', 'AVAILABLE'),
    ('d5d5d5d5-d5d5-d5d5-d5d5-d5d5d5d5d5d5', 'João Pereira',   'MNO-7890', 'AVAILABLE');
