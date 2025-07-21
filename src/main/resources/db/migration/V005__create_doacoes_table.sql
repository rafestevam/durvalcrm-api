-- Criação da tabela de doações
CREATE TABLE doacoes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    associado_id UUID NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('UNICA', 'RECORRENTE')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDENTE', 'PROCESSANDO', 'CONFIRMADA', 'CANCELADA')),
    descricao VARCHAR(500),
    data_doacao TIMESTAMP NOT NULL,
    data_confirmacao TIMESTAMP,
    codigo_transacao VARCHAR(255),
    metodo_pagamento VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_doacao_associado 
        FOREIGN KEY (associado_id) 
        REFERENCES associados(id) 
        ON DELETE RESTRICT
);

-- Índices para melhorar performance
CREATE INDEX idx_doacoes_associado_id ON doacoes(associado_id);
CREATE INDEX idx_doacoes_status ON doacoes(status);
CREATE INDEX idx_doacoes_data_doacao ON doacoes(data_doacao);
CREATE INDEX idx_doacoes_tipo ON doacoes(tipo);
CREATE INDEX idx_doacoes_data_confirmacao ON doacoes(data_confirmacao) WHERE data_confirmacao IS NOT NULL;

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_doacoes_updated_at 
    BEFORE UPDATE ON doacoes 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();