-- senha de todos: 123456 (hash BCrypt)
insert into USUARIOS (id, username, password, role)
    values (100, 'jose@gmail.com', '$2a$10$YTTxONeO8rcdc/BK0KQNM.WO209iSsC4LvCKsbreO4hxeC/kb3xjO','ROLE_ADMIN');
insert into USUARIOS (id, username, password, role)
    values (101, 'maria@gmail.com', '$2a$10$YTTxONeO8rcdc/BK0KQNM.WO209iSsC4LvCKsbreO4hxeC/kb3xjO','ROLE_CLIENTE');
insert into USUARIOS (id, username, password, role)
    values (102, 'joao@gmail.com', '$2a$10$YTTxONeO8rcdc/BK0KQNM.WO209iSsC4LvCKsbreO4hxeC/kb3xjO','ROLE_CLIENTE');

-- maria já tem cliente; joao ainda não
insert into CLIENTES (id, nome, cpf, id_usuario)
    values (10, 'Maria da Silva', '52998224725', 101);
