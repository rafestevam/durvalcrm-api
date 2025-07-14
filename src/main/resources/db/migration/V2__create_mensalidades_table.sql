-- Criação da tabela de mensalidades
CREATE TABLE mensalidades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    associado_id UUID NOT NULL REFERENCES associados(id),
    mes_referencia INT NOT NULL CHECK (mes_referencia >= 1 AND mes_referencia <= 12),
    ano_referencia INT NOT NULL CHECK (ano_referencia >= 2020 AND ano_referencia <= 2030),
    valor NUMERIC(10, 2) NOT NULL CHECK (valor > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    data_vencimento DATE NOT NULL,
    data_pagamento TIMESTAMPTZ,
    qr_code_pix TEXT,
    identificador_pix VARCHAR(50) NOT NULL UNIQUE,
    criado_em TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(associado_id, mes_referencia, ano_referencia)
);

-- Índices para performance
CREATE INDEX idx_mensalidades_periodo ON mensalidades(ano_referencia, mes_referencia);
CREATE INDEX idx_mensalidades_associado ON mensalidades(associado_id);
CREATE INDEX idx_mensalidades_status ON mensalidades(status);
CREATE INDEX idx_mensalidades_vencimento ON mensalidades(data_vencimento);
CREATE INDEX idx_mensalidades_identificador_pix ON mensalidades(identificador_pix);

-- Comentários
COMMENT ON TABLE mensalidades IS 'Tabela para controlar as mensalidades dos associados';
COMMENT ON COLUMN mensalidades.mes_referencia IS 'Mês de referência da mensalidade (1-12)';
COMMENT ON COLUMN mensalidades.ano_referencia IS 'Ano de referência da mensalidade';
COMMENT ON COLUMN mensalidades.valor IS 'Valor da mensalidade (ex: 10.90)';
COMMENT ON COLUMN mensalidades.status IS 'Status: PENDENTE, PAGA, ATRASADA';
COMMENT ON COLUMN mensalidades.identificador_pix IS 'Identificador único para reconciliação de pagamentos PIX';