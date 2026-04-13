DROP TABLE IF EXISTS public.ng_events;

CREATE TABLE IF NOT EXISTS public.ng_events
(
    id bigserial PRIMARY KEY,
    tracingid VARCHAR(255) NOT NULL,
    eventtime timestamp without time zone NOT NULL DEFAULT NOW(),
    status VARCHAR(255),
    event text
);
