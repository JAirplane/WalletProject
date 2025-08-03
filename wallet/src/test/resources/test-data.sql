-- Очистка таблиц (если нужно)
TRUNCATE TABLE transactions CASCADE;
TRUNCATE TABLE wallets CASCADE;

-- Тестовые кошельки
INSERT INTO wallets (id, balance, is_active, created_at, version) VALUES
('550e8400-e29b-41d4-a716-446655440000', 1000.00, true, '2023-01-01 10:00:00', 1),
('550e8400-e29b-41d4-a716-446655440001', 500.50, true, '2023-01-02 11:30:00', 1),
('550e8400-e29b-41d4-a716-446655440002', 0.00, false, '2023-01-03 12:45:00', 1);

-- Тестовые транзакции
INSERT INTO transactions (id, wallet_id, operation_type, amount, created_at) VALUES
('660e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440000', 'DEPOSIT', 1000.00, '2023-01-01 10:05:00'),
('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 'WITHDRAW', 200.50, '2023-01-01 10:30:00'),
('660e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 'DEPOSIT', 500.50, '2023-01-02 11:35:00');