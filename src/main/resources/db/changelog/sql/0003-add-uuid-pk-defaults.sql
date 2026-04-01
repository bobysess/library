-- Add gen_random_uuid() as default for all UUID primary key columns
-- so the DB auto-generates a UUID when id is null on insert

ALTER TABLE author        ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE publisher     ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE category      ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE book          ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE inventory_copy ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE library_member ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE loan          ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE fine          ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE reservation   ALTER COLUMN id SET DEFAULT gen_random_uuid();
