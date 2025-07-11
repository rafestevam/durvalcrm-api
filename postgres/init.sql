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
    criado_em TIMESTAMPTZ DEFAULT NOW()
);

-- Tabela principal para os associados
CREATE TABLE associados (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome_completo VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) UNIQUE NOT NULL, -- Ex: 111.222.333-44 [cite: 18]
    email VARCHAR(255) UNIQUE NOT NULL,
    telefone VARCHAR(20), -- Ex: (11) 99999-1111 [cite: 18]
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMPTZ DEFAULT NOW()
);

-- Tabela para controlar as mensalidades geradas
CREATE TABLE mensalidades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    associado_id UUID NOT NULL REFERENCES associados(id),
    mes_referencia INT NOT NULL, -- Ex: 7 para Julho
    ano_referencia INT NOT NULL, -- Ex: 2025
    valor NUMERIC(10, 2) NOT NULL, -- Ex: 10.90 [cite: 22]
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE', -- PENDENTE, PAGA [cite: 22, 23]
    data_vencimento DATE NOT NULL,
    data_pagamento DATE, -- Preenchida quando o status for 'PAGA' [cite: 22]
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
    descricao_extrato TEXT, -- Para armazenar a descrição original do extrato. Ex: PIX RECEBIDO DE CPF 999.888.777-66 [cite: 8]
    reconciliado BOOLEAN DEFAULT FALSE
);

-- Tabela para o registro de vendas avulsas (Cantina, Bazar, etc.)
CREATE TABLE vendas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    valor NUMERIC(10, 2) NOT NULL,
    origem VARCHAR(50) NOT NULL, -- CANTINA, BAZAR, LIVROS [cite: 2, 3]
    data_venda TIMESTAMPTZ DEFAULT NOW()
);