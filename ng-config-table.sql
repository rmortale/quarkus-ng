DROP TABLE IF EXISTS public.ng_config;

CREATE TABLE IF NOT EXISTS public.ng_config
(
    id bigserial PRIMARY KEY,
    serviceid VARCHAR(255) NOT NULL,
    config text
);
