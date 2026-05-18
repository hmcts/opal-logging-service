--
-- PostgreSQL database dump
--

-- Dumped from database version 17.9 (Debian 17.9-1.pgdg13+1)
-- Dumped by pg_dump version 17.9 (Homebrew)

-- Started on 2026-05-05 17:06:32 BST

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON SCHEMA public IS 'standard public schema';

--
-- Name: pl_category_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.pl_category_enum AS ENUM (
    'COLLECTION',
    'ALTERATION',
    'CONSULTATION',
    'DISCLOSURE',
    'COMBINATION',
    'ERASURE'
);

--
-- Name: pdpo_identifiers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pdpo_identifiers (
    pdpo_identifiers_id bigint NOT NULL,
    business_identifier character varying(250) NOT NULL
);

--
-- Name: COLUMN pdpo_identifiers.pdpo_identifiers_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pdpo_identifiers.pdpo_identifiers_id IS 'Generated primary key for the pdpo_identifiers table';

--
-- Name: COLUMN pdpo_identifiers.business_identifier; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pdpo_identifiers.business_identifier IS 'A unique text representing the business identifier description of the pdpo log';

--
-- Name: pdpo_identifiers_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.pdpo_identifiers_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: pdpo_identifiers_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.pdpo_identifiers_seq OWNED BY public.pdpo_identifiers.pdpo_identifiers_id;

--
-- Name: pdpo_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pdpo_log (
    pdpo_log_id bigint NOT NULL,
    created_by_identifier character varying(50) NOT NULL,
    created_by_identifier_type character varying(30) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    ip_address inet NOT NULL,
    category public.pl_category_enum NOT NULL,
    recipient_identifier character varying(50),
    recipient_identifier_type character varying(30),
    pdpo_identifiers_id bigint NOT NULL
);

--
-- Name: COLUMN pdpo_log.pdpo_log_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pdpo_log.pdpo_log_id IS 'Generated primary key for the access log';

--
-- Name: COLUMN pdpo_log.created_by_identifier; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pdpo_log.created_by_identifier IS 'The identifier of the user/system that this log relates too. e.g. user_id or a system name';

--
-- Name: COLUMN pdpo_log.created_by_identifier_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pdpo_log.created_by_identifier_type IS 'The type of data the created_by_identifier relates to. Examples include OPAL_USER_ID EXTERNAL_SERVICE';

--
-- Name: COLUMN pdpo_log.created_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pdpo_log.created_at IS 'The time this log event was captured. This comes from the inbound JSON and not when this data was saved';

--
-- Name: COLUMN pdpo_log.ip_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pdpo_log.ip_address IS 'The IP address of the user who performed the PDPO';

--
-- Name: COLUMN pdpo_log.category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pdpo_log.category IS 'The category of the log. Examples include COLLECTION ALTERATION CONSULTATION DISCLOSURE (including Transfers) COMBINATION ERASURE';

--
-- Name: COLUMN pdpo_log.recipient_identifier; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pdpo_log.recipient_identifier IS 'The recipient identifier of who is getting the protected information Only recorded where the PDPO Category is Disclosure';

--
-- Name: COLUMN pdpo_log.recipient_identifier_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pdpo_log.recipient_identifier_type IS 'The identifier type of the recipient Examples include DEFENDANT MINOR_CREDITOR OPAL_USER EXTERNAL_SYSTEM Only recorded where the PDPO Category is Disclosure';

--
-- Name: COLUMN pdpo_log.pdpo_identifiers_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pdpo_log.pdpo_identifiers_id IS 'The identifier id from the pdpo_identifiers table';

--
-- Name: pdpo_log_individuals; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pdpo_log_individuals (
    pdpo_log_individuals_id bigint NOT NULL,
    individual_identifier character varying(50) NOT NULL,
    individual_type character varying(30) NOT NULL,
    pdpo_log_id bigint NOT NULL
);

--
-- Name: COLUMN pdpo_log_individuals.pdpo_log_individuals_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pdpo_log_individuals.pdpo_log_individuals_id IS 'Primary key created from a sequence';

--
-- Name: COLUMN pdpo_log_individuals.individual_identifier; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pdpo_log_individuals.individual_identifier IS 'The identifier of the individual whose data was subject to a PDPO';

--
-- Name: COLUMN pdpo_log_individuals.individual_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pdpo_log_individuals.individual_type IS 'The identifier type of the individual whose data was subject to a PDPO';

--
-- Name: COLUMN pdpo_log_individuals.pdpo_log_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pdpo_log_individuals.pdpo_log_id IS 'The pdpo_log row that this individual relates to';

--
-- Name: pdpo_log_individuals_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.pdpo_log_individuals_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: pdpo_log_individuals_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.pdpo_log_individuals_seq OWNED BY public.pdpo_log_individuals.pdpo_log_individuals_id;

--
-- Name: pdpo_log_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.pdpo_log_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: pdpo_log_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.pdpo_log_seq OWNED BY public.pdpo_log.pdpo_log_id;

--
-- Name: pdpo_identifiers pdpo_identifiers_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdpo_identifiers
    ADD CONSTRAINT pdpo_identifiers_pk PRIMARY KEY (pdpo_identifiers_id);

--
-- Name: pdpo_log_individuals pdpo_log_individuals_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdpo_log_individuals
    ADD CONSTRAINT pdpo_log_individuals_pk PRIMARY KEY (pdpo_log_individuals_id);

--
-- Name: pdpo_log pdpo_log_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdpo_log
    ADD CONSTRAINT pdpo_log_pk PRIMARY KEY (pdpo_log_id);

--
-- Name: pdpo_identifiers_business_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pdpo_identifiers_business_idx ON public.pdpo_identifiers USING btree (business_identifier);

--
-- Name: pdpo_log_category_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pdpo_log_category_idx ON public.pdpo_log USING btree (category);

--
-- Name: pdpo_log_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pdpo_log_created_at_idx ON public.pdpo_log USING btree (created_at);

--
-- Name: pdpo_log_created_by_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pdpo_log_created_by_idx ON public.pdpo_log USING btree (created_by_identifier_type, created_by_identifier, pdpo_identifiers_id);

--
-- Name: pdpo_log_identifiers_fk_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pdpo_log_identifiers_fk_idx ON public.pdpo_log USING btree (pdpo_identifiers_id);

--
-- Name: pdpo_log_ip_address_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pdpo_log_ip_address_idx ON public.pdpo_log USING btree (ip_address);

--
-- Name: pdpo_log_recipient_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pdpo_log_recipient_idx ON public.pdpo_log USING btree (recipient_identifier_type, recipient_identifier);

--
-- Name: pli_pdpo_log_id_fk_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pli_pdpo_log_id_fk_idx ON public.pdpo_log_individuals USING btree (pdpo_log_id);

--
-- Name: pli_type_identifier_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pli_type_identifier_idx ON public.pdpo_log_individuals USING btree (individual_type, individual_identifier);

--
-- Name: pdpo_log pdpo_log_identifiers_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdpo_log
    ADD CONSTRAINT pdpo_log_identifiers_fk FOREIGN KEY (pdpo_identifiers_id) REFERENCES public.pdpo_identifiers(pdpo_identifiers_id);

--
-- Name: pdpo_log_individuals pli_pdpo_log_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdpo_log_individuals
    ADD CONSTRAINT pli_pdpo_log_id_fk FOREIGN KEY (pdpo_log_id) REFERENCES public.pdpo_log(pdpo_log_id);

-- Completed on 2026-05-05 17:06:32 BST

--
-- PostgreSQL database dump complete
--

