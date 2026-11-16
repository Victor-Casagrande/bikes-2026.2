-- senha de todos: 123456 (hash BCrypt)
insert into USUARIOS (id, username, password, role)
    values (100, 'jose@gmail.com', '$2a$10$YTTxONeO8rcdc/BK0KQNM.WO209iSsC4LvCKsbreO4hxeC/kb3xjO','ROLE_ADMIN');
insert into USUARIOS (id, username, password, role)
    values (101, 'maria@gmail.com', '$2a$10$YTTxONeO8rcdc/BK0KQNM.WO209iSsC4LvCKsbreO4hxeC/kb3xjO','ROLE_CLIENTE');
insert into USUARIOS (id, username, password, role)
    values (102, 'joao@gmail.com', '$2a$10$YTTxONeO8rcdc/BK0KQNM.WO209iSsC4LvCKsbreO4hxeC/kb3xjO','ROLE_CLIENTE');
insert into USUARIOS (id, username, password, role)
    values (103, 'ana@gmail.com', '$2a$10$YTTxONeO8rcdc/BK0KQNM.WO209iSsC4LvCKsbreO4hxeC/kb3xjO','ROLE_CLIENTE');

-- maria e ana são clientes; joao ainda não tem cadastro de cliente
insert into CLIENTES (id, nome, cpf, id_usuario)
    values (10, 'Maria da Silva', '52998224725', 101);
insert into CLIENTES (id, nome, cpf, id_usuario)
    values (11, 'Ana Souza', '11144477735', 103);

-- BK01 é a única livre
insert into BICICLETAS (id, codigo, modelo, status)
    values (10, 'BK01', 'Caloi Explorer', 'LIVRE');
insert into BICICLETAS (id, codigo, modelo, status)
    values (11, 'BK02', 'Sense Impact', 'ALUGADA');
insert into BICICLETAS (id, codigo, modelo, status)
    values (12, 'BK03', 'Oggi Hacker', 'MANUTENCAO');

-- locação 20: ATIVA, maria com a BK02, retirada há 150 minutos (na devolução: 3 horas iniciadas = R$ 30,00)
insert into LOCACOES (id, id_cliente, id_bicicleta, data_retirada, data_prevista_devolucao, status, cartao_final)
    values (20, 10, 11, DATEADD('MINUTE', -150, LOCALTIMESTAMP), DATEADD('MINUTE', 60, LOCALTIMESTAMP), 'ATIVA', '1111');
-- locação 21: FINALIZADA, ana com a BK03 ontem, durante 2 horas
insert into LOCACOES (id, id_cliente, id_bicicleta, data_retirada, data_prevista_devolucao, data_devolucao, valor, status, cartao_final)
    values (21, 11, 12, DATEADD('HOUR', -26, LOCALTIMESTAMP), DATEADD('HOUR', -24, LOCALTIMESTAMP), DATEADD('HOUR', -24, LOCALTIMESTAMP), 20.00, 'FINALIZADA', '4242');
