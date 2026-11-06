-- senha de todos: 123456 (hash BCrypt, o mesmo formato que o UsuarioService grava)
insert into USUARIOS (id, username, password, role)
    values (100, 'jose@gmail.com', '$2a$10$YTTxONeO8rcdc/BK0KQNM.WO209iSsC4LvCKsbreO4hxeC/kb3xjO','ROLE_ADMIN');
insert into USUARIOS (id, username, password, role)
    values (101, 'maria@gmail.com', '$2a$10$YTTxONeO8rcdc/BK0KQNM.WO209iSsC4LvCKsbreO4hxeC/kb3xjO','ROLE_CLIENTE');
insert into USUARIOS (id, username, password, role)
    values (102, 'joao@gmail.com', '$2a$10$YTTxONeO8rcdc/BK0KQNM.WO209iSsC4LvCKsbreO4hxeC/kb3xjO','ROLE_CLIENTE');
