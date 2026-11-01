-- First create a normal account through POST /api/v1/auth/register.
-- Then run this SQL as a privileged operator, substituting the username:
UPDATE app_users SET role='ADMIN' WHERE username='your-initial-admin';
COMMIT;
-- Do not expose public admin registration. This controlled bootstrap step is intentional.
