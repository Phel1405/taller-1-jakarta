-- Necesario para que WildFly (Narayana) pueda recuperar transacciones XA
-- pendientes en MySQL tras un fallo (XA RECOVER).
GRANT XA_RECOVER_ADMIN ON *.* TO 'dev'@'%';
FLUSH PRIVILEGES;
