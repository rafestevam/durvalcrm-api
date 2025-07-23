-- Adicionar campo metodo_pagamento na tabela mensalidades
ALTER TABLE mensalidades 
ADD COLUMN metodo_pagamento VARCHAR(20) CHECK (metodo_pagamento IN ('PIX', 'DINHEIRO'));

-- Comentário explicativo
COMMENT ON COLUMN mensalidades.metodo_pagamento IS 'Método de pagamento usado: PIX ou DINHEIRO';