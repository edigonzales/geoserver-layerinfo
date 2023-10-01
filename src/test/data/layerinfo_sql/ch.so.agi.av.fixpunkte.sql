SELECT
    hoehe,
    punktzeichen_txt AS "Punktzeichen",
    typ_txt AS "Kategorie",
    nummer as "Fixpunktnummer",
    ST_X(geometrie) AS "Ost",
    ST_Y(geometrie) AS "Nord",
    t_id AS t_id,
    ST_AsGeoJSON(geometrie) AS geometrie
FROM
    agi_mopublic_pub.mopublic_fixpunkt
WHERE 
    ST_Intersects(geometrie, ST_Buffer(ST_GeomFromText('POINT(:x :y)', 2056), :resolution * :TOLERANCE_IN_PIXEL))
