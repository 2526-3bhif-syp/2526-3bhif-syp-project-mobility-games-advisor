CREATE TABLE IF NOT EXISTS users (
                                     id            SERIAL PRIMARY KEY,
                                     username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'user',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    last_login    TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS exercises (
    id           SERIAL PRIMARY KEY,
    uploaded_by  INT          REFERENCES users(id) ON DELETE SET NULL,
    title        VARCHAR(100) NOT NULL,
    description  TEXT,
    category     VARCHAR(50),
    video_url    VARCHAR(255),
    uploaded_at  TIMESTAMP    NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS sammlung (
                                        id    SERIAL PRIMARY KEY,
                                        title VARCHAR(100) NOT NULL,
    owner_id INT REFERENCES users(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS sammlung_exercise (
    sammlung_id  INT REFERENCES sammlung(id) ON DELETE CASCADE,
    exercise_id  INT REFERENCES exercises(id) ON DELETE CASCADE,
    PRIMARY KEY (sammlung_id, exercise_id)
    );

-- 5. Insert data LAST
INSERT INTO exercises (title, description, category, video_url) VALUES
                                                                    ('Gedächtnis-Training', 'Finde passende Bildpaare.', 'Kognition', 'http://localhost:8081/videos/video1.mp4'),
                                                                    ('Sitz-Yoga', 'Einfache Dehnübungen im Sitzen.', 'Mobilität', 'http://localhost:8081/videos/video2.mp4');


UPDATE exercises
SET video_url = REPLACE(video_url, ' ', '%20')
WHERE video_url LIKE '% %';

