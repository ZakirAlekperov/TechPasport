-- Тестовые данные для города Вязьма Смоленской области
-- Координаты в системе МСК-67 зона 2 Смоленской области
-- Основные исторические здания центра Вязьмы

-- 1. Свято-Троицкий кафедральный собор (Соборный холм)
INSERT INTO location_plan_building_coordinates (passport_id, litera, description) 
VALUES ('test-passport-vyazma', 'А', 'Свято-Троицкий кафедральный собор');

INSERT INTO coordinate_point (building_id, point_order, x_coordinate, y_coordinate)
VALUES
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'А'), 0, '2180145.00', '6120235.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'А'), 1, '2180185.00', '6120235.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'А'), 2, '2180185.00', '6120275.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'А'), 3, '2180145.00', '6120275.00');

-- 2. Спасская башня (Вяземская крепость)
INSERT INTO location_plan_building_coordinates (passport_id, litera, description)
VALUES ('test-passport-vyazma', 'Б', 'Спасская башня (век XVII)');

INSERT INTO coordinate_point (building_id, point_order, x_coordinate, y_coordinate)
VALUES
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Б'), 0, '2180050.00', '6120180.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Б'), 1, '2180070.00', '6120180.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Б'), 2, '2180070.00', '6120200.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Б'), 3, '2180050.00', '6120200.00');

-- 3. Церковь Рождества Богородицы (Музей)
INSERT INTO location_plan_building_coordinates (passport_id, litera, description)
VALUES ('test-passport-vyazma', 'В', 'Церковь Рождества Богородицы (Музей)');

INSERT INTO coordinate_point (building_id, point_order, x_coordinate, y_coordinate)
VALUES
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'В'), 0, '2180220.00', '6120150.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'В'), 1, '2180260.00', '6120150.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'В'), 2, '2180260.00', '6120180.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'В'), 3, '2180220.00', '6120180.00');

-- 4. Аркадьевский монастырь (храм Спаса Всемилостивого)
INSERT INTO location_plan_building_coordinates (passport_id, litera, description)
VALUES ('test-passport-vyazma', 'Г', 'Аркадьевский монастырь');

INSERT INTO coordinate_point (building_id, point_order, x_coordinate, y_coordinate)
VALUES
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Г'), 0, '2179980.00', '6120120.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Г'), 1, '2180035.00', '6120120.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Г'), 2, '2180035.00', '6120165.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Г'), 3, '2179980.00', '6120165.00');

-- 5. Иоанно-Предтеченская церковь
INSERT INTO location_plan_building_coordinates (passport_id, litera, description)
VALUES ('test-passport-vyazma', 'Д', 'Иоанно-Предтеченская церковь');

INSERT INTO coordinate_point (building_id, point_order, x_coordinate, y_coordinate)
VALUES
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Д'), 0, '2180100.00', '6120300.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Д'), 1, '2180130.00', '6120300.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Д'), 2, '2180130.00', '6120325.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Д'), 3, '2180100.00', '6120325.00');

-- 6. Администрация города
INSERT INTO location_plan_building_coordinates (passport_id, litera, description)
VALUES ('test-passport-vyazma', 'Е', 'Администрация города Вязьма');

INSERT INTO coordinate_point (building_id, point_order, x_coordinate, y_coordinate)
VALUES
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Е'), 0, '2180190.00', '6120080.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Е'), 1, '2180240.00', '6120080.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Е'), 2, '2180240.00', '6120110.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Е'), 3, '2180190.00', '6120110.00');

-- 7. Центральная площадь - здание Торговых рядов
INSERT INTO location_plan_building_coordinates (passport_id, litera, description)
VALUES ('test-passport-vyazma', 'Ж', 'Торговые ряды (центральная площадь)');

INSERT INTO coordinate_point (building_id, point_order, x_coordinate, y_coordinate)
VALUES
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Ж'), 0, '2180280.00', '6120190.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Ж'), 1, '2180340.00', '6120190.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Ж'), 2, '2180340.00', '6120215.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'Ж'), 3, '2180280.00', '6120215.00');

-- 8. Вокзал Вязьма
INSERT INTO location_plan_building_coordinates (passport_id, litera, description)
VALUES ('test-passport-vyazma', 'З', 'Железнодорожный вокзал');

INSERT INTO coordinate_point (building_id, point_order, x_coordinate, y_coordinate)
VALUES
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'З'), 0, '2180050.00', '6119950.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'З'), 1, '2180130.00', '6119950.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'З'), 2, '2180130.00', '6119985.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'З'), 3, '2180050.00', '6119985.00');

-- 9. Центральная библиотека
INSERT INTO location_plan_building_coordinates (passport_id, litera, description)
VALUES ('test-passport-vyazma', 'И', 'Центральная библиотека');

INSERT INTO coordinate_point (building_id, point_order, x_coordinate, y_coordinate)
VALUES
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'И'), 0, '2180160.00', '6120340.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'И'), 1, '2180200.00', '6120340.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'И'), 2, '2180200.00', '6120365.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'И'), 3, '2180160.00', '6120365.00');

-- 10. Центральный парк культуры
INSERT INTO location_plan_building_coordinates (passport_id, litera, description)
VALUES ('test-passport-vyazma', 'К', 'Павильон центрального парка');

INSERT INTO coordinate_point (building_id, point_order, x_coordinate, y_coordinate)
VALUES
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'К'), 0, '2180300.00', '6120260.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'К'), 1, '2180335.00', '6120260.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'К'), 2, '2180335.00', '6120285.00'),
  ((SELECT id FROM location_plan_building_coordinates WHERE passport_id = 'test-passport-vyazma' AND litera = 'К'), 3, '2180300.00', '6120285.00');

-- Добавление информации о ситуационном плане
INSERT INTO location_plan (passport_id, scale_denominator, executor_name, plan_date, notes)
VALUES (
  'test-passport-vyazma',
  '500',
  'Тестовые данные',
  CURRENT_DATE,
  'Тестовые данные для исторического центра города Вязьма, Смоленской области. Координаты в системе МСК-67 зона 2. Включены основные исторические здания: Свято-Троицкий собор, Спасская башня, Аркадьевский монастырь и другие объекты.'
);

COMMIT;
