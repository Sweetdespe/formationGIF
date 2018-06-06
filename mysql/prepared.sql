USE pyrates;

PREPARE toto FROM 'SELECT * FROM famous_pyrates WHERE id=?';
SET @id = 1;
EXECUTE toto USING @id;