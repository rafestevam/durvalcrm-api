-- Atualizar mensalidades já pagas para terem um método de pagamento
-- Mensalidades são SEMPRE pagas via PIX (100%)
UPDATE mensalidades 
SET metodo_pagamento = 'PIX' 
WHERE status = 'PAGA' AND metodo_pagamento IS NULL;

-- Comentário explicativo
COMMENT ON COLUMN mensalidades.metodo_pagamento IS 'Método de pagamento usado: PIX ou DINHEIRO. Mensalidades são sempre PIX (100%)';