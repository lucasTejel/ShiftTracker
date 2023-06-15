


CREATE TABLE usuarios (
	id_usuario INT PRIMARY KEY,
	nombre VARCHAR(16) NOT NULL,
    email VARCHAR(100) NOT NULL,
    telefono VARCHAR(15),
    contrasena VARCHAR(100) NOT NULL
);

CREATE TABLE usuarios_comunes (
	id_usuario INT PRIMARY KEY,
    estado BOOL NOT NULL DEFAULT 0,
    CONSTRAINT fk_usuarios_comunes_usuarios FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
);

CREATE TABLE usuarios_administradores (
	id_usuario INT PRIMARY KEY,
	cargo VARCHAR(100),
    CONSTRAINT fk_usuarios_administradores_usuarios FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
);

CREATE TABLE notificaciones (
	id_usuario INT,
	fecha_hora_recibo TIMESTAMP,
    descripcion VARCHAR(500),
    CONSTRAINT pk_notificaciones PRIMARY KEY (id_usuario, fecha_hora_recibo),
    CONSTRAINT fk_notificaciones_usuarios_administradores FOREIGN KEY (id_usuario) REFERENCES usuarios_administradores (id_usuario)
);

CREATE TABLE grupos (
	id_grupo INT PRIMARY KEY,
    nombre VARCHAR(16) NOT NULL,
    descripcion VARCHAR(100),
	horario VARCHAR(100),
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    id_usuario_administrador INT,
    CONSTRAINT fk_grupos_usuarios_administradores FOREIGN KEY (id_usuario_administrador) REFERENCES usuarios_administradores (id_usuario)
);

CREATE TABLE usuarios_grupos (
	id_usuario INT,
    id_grupo INT,
    CONSTRAINT pk_usuarios_grupos PRIMARY KEY (id_usuario, id_grupo),
    CONSTRAINT fk_usuarios_grupos_grupos FOREIGN KEY (id_grupo) REFERENCES grupos (id_grupo),
    CONSTRAINT fk_usuarios_grupos_usuarios_comunes FOREIGN KEY (id_usuario) REFERENCES usuarios_comunes (id_usuario)
);

CREATE TABLE asistencias (
	id_usuario INT,
    id_grupo INT,
    fecha_hora_entrada TIMESTAMP,
    CONSTRAINT pk_asistencias PRIMARY KEY (id_usuario, id_grupo, fecha_hora_entrada),
    CONSTRAINT fk_asistencias_grupos FOREIGN KEY (id_grupo) REFERENCES grupos (id_grupo),
    CONSTRAINT fk_asistencias_usuarios_comunes FOREIGN KEY (id_usuario) REFERENCES usuarios_comunes (id_usuario)
);

CREATE TABLE periodos_festivos (
	id_grupo INT,
    fecha_inicio DATE,
    fecha_fin DATE,
    CONSTRAINT pk_periodos_festivos PRIMARY KEY (id_grupo, fecha_inicio, fecha_fin),
    CONSTRAINT fk_periodos_festivos_grupos FOREIGN KEY (id_grupo) REFERENCES grupos (id_grupo)
);

CREATE TABLE periodos_vacacionales (
	id_usuario INT,
	id_grupo INT,
	fecha_inicio DATE,
    fecha_fin DATE,
    CONSTRAINT pk_periodos_vacacionales PRIMARY KEY (id_usuario, id_grupo, fecha_inicio, fecha_fin),
    CONSTRAINT fk_periodos_vacacionales_usuarios_comunes FOREIGN KEY (id_usuario) REFERENCES usuarios_comunes (id_usuario),
	CONSTRAINT fk_periodos_vacacionales_grupos FOREIGN KEY (id_grupo) REFERENCES grupos (id_grupo)
);



-- INSERCIONES

INSERT INTO usuarios (id_usuario, nombre, email, telefono, contrasena)
VALUES 
(1, 'jperez', 'juanperez@gmail.com', '611134567', 'contrasena1'),
(2, 'mgarcia', 'mariagarcia@hotmail.com', '611345678', 'contrasena1'),
(3, 'cramirez', 'carlosramirez@yahoo.com', '633456789', 'contrasena3'),
(4, 'amartinez', 'anamartinez@gmail.com', '644567890', 'contrasena4'),
(5, 'psanchez', 'pedrosanchez@hotmail.com', '655678901', 'contrasena5'),
(6, 'lgomez', 'luisagomez@yahoo.com', '666789011', 'contrasena6'),
(7, 'mtorres', 'migueltorres@gmail.com', '677890113', 'contrasena7'),
(8, 'phernandez', 'paolahernandez@hotmail.com', '688901134', 'contrasena8'),
(9, 'fdiaz', 'fernandodiaz@yahoo.com', '699011345', 'contrasena9'),
(10, 'srodriguez', 'sofia.rodriguez@gmail.com', '610134567', 'contrasena10'),
(11, 'jlopez', 'jorgelopez@gmail.com', '611345678', 'contrasena11'),
(12, 'lreyes', 'laurareyes@hotmail.com', '611456789', 'contrasena11'),
(13, 'dgarcia', 'danielgarcia@yahoo.com', '613567890', 'contrasena13'),
(14, 'vtorres', 'valeriatorres@gmail.com', '614678901', 'contrasena14'),
(15, 'aperez', 'alejandroperez@hotmail.com', '615789011', 'contrasena15'),
(16, 'cramirez1', 'carmenramirez@gmail.com', '616890113', 'contrasena16'),
(17, 'hflores', 'hectorflores@hotmail.com', '617901134', 'contrasena17'),
(18, 'ghernandez', 'gabrielahernandez@yahoo.com', '618011345', 'contrasena18'),
(19, 'rdiaz', 'ricardodiaz@gmail.com', '619011345', 'contrasena19'),
(20, 'srodriguez1', 'silvia.rodriguez@yahoo.com', '610111345', 'contrasena10');


INSERT INTO usuarios_comunes (id_usuario, estado)
VALUES
(1, 0),
(2, 1),
(3, 0),
(4, 1),
(5, 0),
(6, 1),
(7, 0),
(8, 1),
(9, 0),
(10, 1);


INSERT INTO usuarios_administradores (id_usuario, cargo)
VALUES
(11, 'Gerente de marketing'),
(12, 'Jefe de ventas'),
(13, 'Director financiero'),
(14, 'Coordinador de proyectos'),
(15, 'Gerente de recursos humanos'),
(16, 'Supervisor de producción'),
(17, 'Jefe de tecnología'),
(18, 'Director creativo'),
(19, 'Coordinador de logística'),
(20, 'Gerente de operaciones');


INSERT INTO grupos (id_grupo, nombre, descripcion, horario, fecha_inicio, fecha_fin, id_usuario_administrador)
VALUES
(1, 'Grupo de yoga', 'Clases de yoga para principiantes', 'M-J; 18:00-19:00', '2023-06-01', '2023-08-31', 11),
(2, 'Grupo senderismo', 'Excursiones por la montaña', 'S; 8:00-14:00', '2023-06-15', '2023-10-31', 12),
(3, 'Grupo de lectura', 'Compartiendo nuestras lecturas favoritas', 'V; 19:00-20:30', '2023-07-01', '2023-09-30', 13),
(4, 'Grupo de cocina', 'Aprendiendo a cocinar platos internacionales', 'L-V; 10:00-12:00', '2023-07-15', '2023-10-31', 14),
(5, 'Grupo de foto', 'Salidas fotográficas', 'S; 16:00-19:00', '2023-08-01', '2023-11-30', 15),
(6, 'Grupo de música', 'Ensamblaje de músicos', 'M-J; 20:00-22:00', '2023-08-15', '2023-12-15', 16),
(7, 'Grupo de idiomas', 'Conversación en inglés', 'M; 16:00-18:00', '2023-09-01', '2023-12-31', 17),
(8, 'Grupo de teatro', 'Improvisación y actuación', 'M-J; 18:00-20:00', '2023-09-15', '2023-12-15', 18),
(9, 'Grupo de baile', 'Clases de salsa y bachata', 'V; 20:00-22:00', '2023-10-01', '2023-12-31', 19),
(10, 'Grupo de arte', 'Pintura y dibujo', 'S; 10:00-12:00', '2023-10-15', '2023-12-15', 20);


INSERT INTO usuarios_grupos (id_usuario, id_grupo) VALUES
(1, 1), (2, 1), (3, 1), (4, 1), (5, 1),
(1, 2), (3, 2), (5, 2), (7, 2), (9, 2),
(2, 3), (4, 3), (6, 3), (8, 3), (10, 3),
(1, 4), (3, 4), (5, 4), (7, 4), (9, 4),
(2, 5), (4, 5), (6, 5), (8, 5), (10, 5), 
(1, 6), (2, 6), (3, 6), (4, 6), (5, 6), (6, 6), (7, 6), (8, 6), (9, 6), (10, 6),
(1, 7), (2, 7), (3, 7), (4, 7), (5, 7), (6, 7), (7, 7), (8, 7), (9, 7), (10, 7),
(1, 8), (2, 8), (3, 8), (4, 8), (5, 8), (6, 8), (7, 8), (8, 8), (9, 8), (10, 8),
(1, 9), (2, 9), (3, 9), (4, 9), (5, 9), (6, 9), (7, 9), (8, 9), (9, 9), (10, 9),
(1, 10), (2, 10), (3, 10), (4, 10), (5, 10), (6, 10), (7, 10), (8, 10), (9, 10), (10, 10);


INSERT INTO periodos_festivos (id_grupo, fecha_inicio, fecha_fin)
VALUES
    (1, '2023-12-25', '2023-12-31'),
    (1, '2024-04-01', '2024-04-05'),
    (1, '2024-07-04', '2024-07-07'),
    (1, '2024-11-23', '2024-11-25'),
    (1, '2024-12-24', '2024-12-31'),
    (2, '2023-11-01', '2023-11-02'),
    (2, '2023-12-24', '2023-12-25'),
    (2, '2024-03-17', '2024-03-17'),
    (2, '2024-06-15', '2024-06-16'),
    (2, '2024-12-24', '2024-12-25'),
    (3, '2023-09-01', '2023-09-02'),
    (3, '2023-11-11', '2023-11-11'),
    (3, '2024-01-15', '2024-01-17'),
    (3, '2024-03-20', '2024-03-21'),
    (3, '2024-10-01', '2024-10-05'),
	(4, '2024-01-01', '2024-01-01'),
    (4, '2024-05-01', '2024-05-01'),
    (5, '2023-10-31', '2023-10-31'),
    (5, '2024-01-06', '2024-01-06'),
    (6, '2023-12-24', '2023-12-25'),
    (6, '2024-02-14', '2024-02-14'),
    (7, '2023-11-23', '2023-11-23'),
    (7, '2024-04-21', '2024-04-21'),
    (8, '2023-10-09', '2023-10-09'),
    (8, '2024-06-21', '2024-06-21'),
    (9, '2023-11-02', '2023-11-02'),
    (9, '2024-02-21', '2024-02-21'),
    (10, '2023-12-31', '2024-01-01'),
    (10, '2024-03-08', '2024-03-08');