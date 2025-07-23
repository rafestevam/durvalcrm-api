-- Atualizar mensalidades já pagas para terem um método de pagamento
-- Usar PIX como padrão para mensalidades já pagas (assumindo que a maioria foi PIX)
UPDATE mensalidades 
SET metodo_pagamento = 'PIX' 
WHERE status = 'PAGA' AND metodo_pagamento IS NULL;

-- Comentário explicativo
COMMENT ON COLUMN mensalidades.metodo_pagamento IS 'Método de pagamento usado: PIX ou DINHEIRO. PIX é o padrão para mensalidades já pagas';