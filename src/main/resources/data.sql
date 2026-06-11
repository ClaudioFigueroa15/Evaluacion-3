-- 1. Insertar Géneros Iniciales
INSERT INTO generos (nombre, descripcion) VALUES ('Acción', 'Películas con alta intensidad, explosiones y combates.');
INSERT INTO generos (nombre, descripcion) VALUES ('Ciencia Ficción', 'Historias con elementos futuristas, espaciales o tecnología avanzada.');
INSERT INTO generos (nombre, descripcion) VALUES ('Drama', 'Obras enfocadas en el desarrollo profundo y emocional de personajes.');
INSERT INTO generos (nombre, descripcion) VALUES ('Comedia', 'Contenido diseñado principalmente para hacer reír y entretener.');
INSERT INTO generos (nombre, descripcion) VALUES ('Terror', 'Películas que buscan causar miedo, tensión o suspenso en el espectador.');

-- 2. Insertar Películas Iniciales
INSERT INTO peliculas (titulo, anio_estreno, duracion, id_genero) VALUES ('Inception', 2010, 148, 2);
INSERT INTO peliculas (titulo, anio_estreno, duracion, id_genero) VALUES ('Gladiator', 2000, 155, 1);
INSERT INTO peliculas (titulo, anio_estreno, duracion, id_genero) VALUES ('Superbad', 2007, 113, 4);

-- 3. Insertar Valoraciones Iniciales
INSERT INTO valoraciones (puntaje, comentario, id_pelicula) VALUES (9, 'Una obra maestra absoluta de la ciencia ficción moderna.', 1);
INSERT INTO valoraciones (puntaje, comentario, id_pelicula) VALUES (10, 'Épica y emocionante. La mejor película de acción.', 2);
INSERT INTO valoraciones (puntaje, comentario, id_pelicula) VALUES (8, 'Muy divertida, ideal para ver un fin de semana.', 3);