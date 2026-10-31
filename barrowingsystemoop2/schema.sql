CREATE DATABASE IF NOT EXISTS item_borrowing;
USE item_borrowing;

CREATE TABLE users (
  user_id    INT AUTO_INCREMENT PRIMARY KEY,
  username   VARCHAR(50)  NOT NULL UNIQUE,
  password   VARCHAR(100) NOT NULL,
  full_name  VARCHAR(100) NOT NULL,
  role       ENUM('ADMIN','BORROWER') NOT NULL
);

CREATE TABLE items (
  item_id    INT AUTO_INCREMENT PRIMARY KEY,
  name       VARCHAR(100) NOT NULL,
  category   VARCHAR(50)  NOT NULL,
  status     ENUM('AVAILABLE','BORROWED') NOT NULL DEFAULT 'AVAILABLE'
);

CREATE TABLE borrow_requests (
  request_id    INT AUTO_INCREMENT PRIMARY KEY,
  item_id       INT  NOT NULL,
  borrower_id   INT  NOT NULL,
  request_date  DATE NOT NULL,
  due_date      DATE NOT NULL,
  return_date   DATE NULL,
  status        ENUM('PENDING','APPROVED','REJECTED','RETURNED')
                NOT NULL DEFAULT 'PENDING',
  FOREIGN KEY (item_id)     REFERENCES items(item_id),
  FOREIGN KEY (borrower_id) REFERENCES users(user_id)
);

INSERT INTO users (username, password, full_name, role) VALUES
  ('admin',    'admin123', 'CCS Admin',      'ADMIN'),
  ('student1', 'pass123',  'Juan Dela Cruz', 'BORROWER'),
  ('student2', 'pass123',  'Maria Santos',   'BORROWER');

INSERT INTO items (name, category) VALUES
  ('Epson Projector',   'Projector'),
  ('Extension Cord 5m', 'Electrical'),
  ('HDMI Cable 3m',     'Cable'),
  ('Multimeter',        'Lab tool');
