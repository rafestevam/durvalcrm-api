-- Criação da tabela de vendas
CREATE TABLE vendas (
    id UUID PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL,
    valor DECIMAL(10,2) NOT NULL CHECK (valor > 0),
    origem VARCHAR(20) NOT NULL CHECK (origem IN ('CANTINA', 'BAZAR', 'LIVROS')),
    data_venda TIMESTAMP WITH TIME ZONE NOT NULL,
    observacoes VARCHAR(500),
    associado_id UUID NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    
    -- Índices para otimização de consultas
    CONSTRAINT fk_venda_associado FOREIGN KEY (associado_id) REFERENCES associados(id)
);

-- Índices para melhorar performance
CREATE INDEX idx_vendas_data_venda ON vendas(data_venda);
CREATE INDEX idx_vendas_origem ON vendas(origem);
CREATE INDEX idx_vendas_associado_id ON vendas(associado_id);
CREATE INDEX idx_vendas_criado_em ON vendas(criado_em);

-- Índice composto para consultas por período e origem
CREATE INDEX idx_vendas_periodo_origem ON vendas(data_venda, origem);
CREATE INDEX idx_vendas_associado_periodo ON vendas(associado_id, data_venda);

-- Comentários das colunas
COMMENT ON TABLE vendas IS 'Tabela para armazenar as vendas realizadas pelos associados';
COMMENT ON COLUMN vendas.id IS 'Identificador único da venda';
COMMENT ON COLUMN vendas.descricao IS 'Descrição da venda';
COMMENT ON COLUMN vendas.valor IS 'Valor da venda em reais';
COMMENT ON COLUMN vendas.origem IS 'Origem da venda (CANTINA, BAZAR, LIVROS)';
COMMENT ON COLUMN vendas.data_venda IS 'Data e hora da venda';
COMMENT ON COLUMN vendas.observacoes IS 'Observações adicionais sobre a venda';
COMMENT ON COLUMN vendas.associado_id IS 'ID do associado que realizou a venda';
COMMENT ON COLUMN vendas.criado_em IS 'Data e hora de criação do registro';
COMMENT ON COLUMN vendas.atualizado_em IS 'Data e hora da última atualização do registro';