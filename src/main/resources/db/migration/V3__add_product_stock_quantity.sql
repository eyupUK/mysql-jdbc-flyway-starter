ALTER TABLE products
  ADD COLUMN stock_quantity INT NOT NULL DEFAULT 0 AFTER price,
  ADD CONSTRAINT chk_products_stock_quantity CHECK (stock_quantity >= 0);

CREATE INDEX idx_products_stock_quantity ON products(stock_quantity);
