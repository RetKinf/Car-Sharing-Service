INSERT INTO rentals (id, rental_date, return_date, car_id, user_id) VALUES (1, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 1, 2)
