-- Adiciona coluna forma_pagamento na tabela vendas
ALTER TABLE vendas 
ADD COLUMN forma_pagamento VARCHAR(20) CHECK (forma_pagamento IN ('PIX', 'DINHEIRO'));

-- Atualiza registros existentes com valor padrão
UPDATE vendas SET forma_pagamento = 'DINHEIRO' WHERE forma_pagamento IS NULL;

-- Torna a coluna obrigatória
ALTER TABLE vendas ALTER COLUMN forma_pagamento SET NOT NULL;

-- Adiciona índice para otimização
CREATE INDEX idx_vendas_forma_pagamento ON vendas(forma_pagamento);

-- Comentário da coluna
COMMENT ON COLUMN vendas.forma_pagamento IS 'Forma de pagamento da venda (PIX ou DINHEIRO)';