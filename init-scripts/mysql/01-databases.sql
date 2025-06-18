 -- Crear la base de datos para el servicio de productos si no existe
    CREATE DATABASE IF NOT EXISTS products_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    -- Crear la base de datos para el servicio de pedidos si no existe
    CREATE DATABASE IF NOT EXISTS orders_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    -- Crear la base de datos para el servicio de carrito si no existe
    CREATE DATABASE IF NOT EXISTS carts_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    
    -- Crear la base de datos para el servicio de usuarios si no existe
    CREATE DATABASE IF NOT EXISTS users_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    