-- Insertamos el usuario Admin:
-- username = admin, email = admin@bar.com, password = admin
INSERT IGNORE INTO users (username, email, password, locked, disabled) VALUES ('admin', 'admin@bar.com', '$2a$10$jd7kIQhu5jg11bZ.c8qkf.G9.EYOPfNnCRNwydKCRMTSs.2Z0bsoG', 0, 0);
INSERT IGNORE INTO user_role (username, role) VALUES ('admin', 'ADMIN');

-- Insertamos el usuario Waiter:
-- username = mesero, email = mesero@bar.com, password = mesero
INSERT IGNORE INTO users (username, email, password, locked, disabled) VALUES ('mesero', 'mesero@bar.com', '$2a$10$Xz4Q.U3EN3RwcbWQfqwNrOnFNs22Z7yBbEEyvBzwp/u5oP41yNcqy', 0, 0);
INSERT IGNORE INTO user_role (username, role) VALUES ('mesero', 'WAITER');
