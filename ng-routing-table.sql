DROP TABLE IF EXISTS public.ng_routing;

CREATE TABLE IF NOT EXISTS public.ng_routing
(
    id bigserial PRIMARY KEY,
    serviceid VARCHAR(255) NOT NULL,
    transfertype VARCHAR(255) NOT NULL,
    transferconfig text
);
