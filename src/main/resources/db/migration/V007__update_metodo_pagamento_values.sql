-- Update existing payment methods to use the new enum values
UPDATE doacoes 
SET metodo_pagamento = 'PIX' 
WHERE metodo_pagamento = 'PIX';

UPDATE doacoes 
SET metodo_pagamento = 'DINHEIRO' 
WHERE metodo_pagamento IN ('Dinheiro', 'Cash', 'Cartão de Crédito', 'Cartão de Débito', 'Boleto', 'Transferência');

-- Add constraint to ensure only valid payment methods are used
ALTER TABLE doacoes 
ADD CONSTRAINT chk_metodo_pagamento 
CHECK (metodo_pagamento IN ('PIX', 'DINHEIRO') OR metodo_pagamento IS NULL);