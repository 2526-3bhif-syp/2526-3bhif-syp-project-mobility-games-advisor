CREATE TABLE IF NOT EXISTS exercises (
    id          SERIAL PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    description TEXT,
    category    VARCHAR(50),
    video_url   VARCHAR(255)
);

INSERT INTO exercises (title, description, category, video_url) VALUES
                                                                    ('Gedächtnis-Training', 'Finde passende Bildpaare.',          'Kognition',    'http://localhost:8081/videos/video1.mp4'),
                                                                    ('Sitz-Yoga',           'Einfache Dehnübungen im Sitzen.',    'Mobilität',    'http://localhost:8081/videos/video2.mp4');
