-- Remove associado_id and observacoes columns from vendas table
ALTER TABLE vendas 
    DROP CONSTRAINT IF EXISTS vendas_associado_id_fkey,
    DROP COLUMN IF EXISTS associado_id,
    DROP COLUMN IF EXISTS observacoes;

-- Drop the index on associado_id
DROP INDEX IF EXISTS idx_vendas_associado_id;