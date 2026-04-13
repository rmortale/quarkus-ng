DROP TABLE IF EXISTS public.ng_sftp;

CREATE TABLE IF NOT EXISTS public.ng_sftp
(
    id bigserial PRIMARY KEY,
    serviceid VARCHAR(255) NOT NULL,
    tracingid VARCHAR(255) NOT NULL,
    eventtime timestamp without time zone NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'RECEIVED',
    statustime timestamp without time zone NOT NULL,
    minioevent text NOT NULL,
    transferconfig text NOT NULL
);
