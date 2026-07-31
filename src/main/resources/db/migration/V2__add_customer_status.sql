ALTER TABLE customers
  ADD COLUMN status ENUM('ACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE' AFTER last_name;

CREATE INDEX idx_customers_status ON customers(status);
