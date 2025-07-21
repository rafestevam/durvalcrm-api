-- Tornar associado_id opcional para permitir doações anônimas
ALTER TABLE doacoes 
ALTER COLUMN associado_id DROP NOT NULL;

-- Remover a constraint de foreign key restritiva
ALTER TABLE doacoes 
DROP CONSTRAINT fk_doacao_associado;

-- Recriar a constraint permitindo NULL com CASCADE para limpeza
ALTER TABLE doacoes 
ADD CONSTRAINT fk_doacao_associado 
    FOREIGN KEY (associado_id) 
    REFERENCES associados(id) 
    ON DELETE SET NULL;