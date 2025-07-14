-- =================================================================
-- SCRIPT DE CRIAÇÃO DO SCHEMA PARA O BANCO 'durvalcrm_dev'
-- PRÉ-REQUISITO: Este script deve ser executado APÓS a criação
-- do banco 'durvalcrm_dev' e conectado a ele.
-- =================================================================

-- Tabela para usuários do sistema (administradores)
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    criado_em TIMESTAMP DEFAULT NOW()
);

-- Tabela principal para os associados
CREATE TABLE associados (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome_completo VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) UNIQUE NOT NULL, -- Ex: 111.222.333-44
    email VARCHAR(255) UNIQUE NOT NULL,
    telefone VARCHAR(20), -- Ex: (11) 99999-1111
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT NOW()
);

-- Tabela para controlar as mensalidades geradas
CREATE TABLE mensalidades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    associado_id UUID NOT NULL REFERENCES associados(id),
    mes_referencia INT NOT NULL CHECK (mes_referencia >= 1 AND mes_referencia <= 12),
    ano_referencia INT NOT NULL CHECK (ano_referencia >= 2020 AND ano_referencia <= 2030),
    valor NUMERIC(10, 2) NOT NULL CHECK (valor > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    data_vencimento DATE NOT NULL,
    data_pagamento TIMESTAMP,
    qr_code_pix TEXT,
    identificador_pix VARCHAR(50) NOT NULL UNIQUE,
    criado_em TIMESTAMP DEFAULT NOW(),
    UNIQUE(associado_id, mes_referencia, ano_referencia)
);

-- Tabela para registrar pagamentos (para reconciliação)
CREATE TABLE pagamentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mensalidade_id UUID REFERENCES mensalidades(id), -- Pode ser nulo para transações não identificadas
    data_pagamento DATE NOT NULL,
    valor_pago NUMERIC(10, 2) NOT NULL,
    metodo_pagamento VARCHAR(50), -- PIX, DINHEIRO, CARTAO
    origem_informacao VARCHAR(50) NOT NULL, -- MANUAL, EXTRATO_CSV, EXTRATO_OFX
    descricao_extrato TEXT, -- Para armazenar a descrição original do extrato
    reconciliado BOOLEAN DEFAULT FALSE,
    criado_em TIMESTAMP DEFAULT NOW()
);

-- Tabela para o registro de vendas avulsas (Cantina, Bazar, etc.)
CREATE TABLE vendas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    valor NUMERIC(10, 2) NOT NULL,
    origem VARCHAR(50) NOT NULL, -- CANTINA, BAZAR, LIVROS
    data_venda TIMESTAMP DEFAULT NOW(),
    criado_em TIMESTAMP DEFAULT NOW()
);

-- Índices para performance
CREATE INDEX idx_mensalidades_periodo ON mensalidades(ano_referencia, mes_referencia);
CREATE INDEX idx_mensalidades_associado ON mensalidades(associado_id);
CREATE INDEX idx_mensalidades_status ON mensalidades(status);
CREATE INDEX idx_mensalidades_vencimento ON mensalidades(data_vencimento);
CREATE INDEX idx_mensalidades_identificador_pix ON mensalidades(identificador_pix);
CREATE INDEX idx_pagamentos_mensalidade ON pagamentos(mensalidade_id);
CREATE INDEX idx_pagamentos_data ON pagamentos(data_pagamento);
CREATE INDEX idx_vendas_origem ON vendas(origem);
CREATE INDEX idx_vendas_data ON vendas(data_venda);