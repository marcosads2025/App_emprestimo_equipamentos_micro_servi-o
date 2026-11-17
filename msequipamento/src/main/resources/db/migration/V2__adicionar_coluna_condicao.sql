-- Adiciona a coluna condicao para rastrear o estado físico do equipamento
-- Valor padrão 'BOM' para registros existentes e novos
ALTER TABLE tb_equipamentos 
ADD COLUMN condicao VARCHAR(20) NOT NULL DEFAULT 'BOM';
