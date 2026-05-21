-- Actualiza los precios de los viajes de 2026 con tarifas diferenciadas
-- segun la calidad de cada viaje (categoria del hotel, duracion y destino).
--
-- Niño ~= 66% del precio adulto  |  Senior ~= 89% del precio adulto.
--
-- Compatible con MySQL y H2.
-- Uso MySQL:  mysql -u <user> -p <database> < scripts/update-trip-prices-2026.sql

UPDATE trips SET price_adult = 2490.00, price_child = 1640.00, price_senior = 2210.00 WHERE destination = 'París';
UPDATE trips SET price_adult = 1690.00, price_child = 1110.00, price_senior = 1500.00 WHERE destination = 'Roma';
UPDATE trips SET price_adult = 3690.00, price_child = 2430.00, price_senior = 3280.00 WHERE destination = 'Tokio';
UPDATE trips SET price_adult = 3190.00, price_child = 2100.00, price_senior = 2840.00 WHERE destination = 'Nueva York';
UPDATE trips SET price_adult = 2190.00, price_child = 1440.00, price_senior = 1950.00 WHERE destination = 'Santorini';
UPDATE trips SET price_adult = 1790.00, price_child = 1180.00, price_senior = 1590.00 WHERE destination = 'Londres';
UPDATE trips SET price_adult = 3990.00, price_child = 2620.00, price_senior = 3550.00 WHERE destination = 'Dubái';
UPDATE trips SET price_adult = 2890.00, price_child = 1900.00, price_senior = 2570.00 WHERE destination = 'Bali';
UPDATE trips SET price_adult = 1490.00, price_child =  980.00, price_senior = 1320.00 WHERE destination = 'Praga';
UPDATE trips SET price_adult = 4790.00, price_child = 3150.00, price_senior = 4260.00 WHERE destination = 'Maldivas';
