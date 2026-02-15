CREATE TABLE IF NOT EXISTS items
(
    id          BIGINT         NOT NULL PRIMARY KEY,
    title       VARCHAR(50)    NOT NULL,
    description VARCHAR(255)   NOT NULL,
    price       NUMERIC(12, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS items_images
(
    item_id      BIGINT PRIMARY KEY REFERENCES items (id) ON DELETE CASCADE,
    file_name    VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size    BIGINT       NOT NULL,
    file_data    BYTEA        NOT NULL
);

CREATE TABLE IF NOT EXISTS orders
(
    order_id      BIGINT  NOT NULL,
    item_id       BIGINT  NOT NULL REFERENCES items (id) ON DELETE CASCADE,
    item_quantity INTEGER NOT NULL,
    PRIMARY KEY (order_id, item_id)
);