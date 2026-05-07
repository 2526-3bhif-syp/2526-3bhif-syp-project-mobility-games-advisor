CREATE TABLE IF NOT EXISTS exercises (
    id          SERIAL PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    description TEXT,
    category    VARCHAR(50),
    video_url   VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS sammlung (
    id    SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS sammlung_exercise (
    sammlung_id  INT REFERENCES sammlung(id) ON DELETE CASCADE,
    exercise_id  INT REFERENCES exercises(id) ON DELETE CASCADE,
    PRIMARY KEY (sammlung_id, exercise_id)
);

INSERT INTO exercises (title, description, category, video_url) VALUES
                                                                    ('Gedächtnis-Training', 'Finde passende Bildpaare.',          'Kognition',    'http://localhost:8081/videos/video1.mp4'),
                                                                    ('Sitz-Yoga',           'Einfache Dehnübungen im Sitzen.',    'Mobilität',    'http://localhost:8081/videos/video2.mp4');
