CREATE OR REPLACE LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION menu_check_procedure() 
   RETURNS "trigger" AS 
	$BODY$
	BEGIN
	   --Checks itemName constraints
	   IF NEW.itemName IS NULL THEN
	   RAISE EXCEPTION 'Item name cannot be null/empty';
	   END IF;
	   IF LENGTH(NEW.itemName) > 50 THEN
	   RAISE EXCEPTION 'Item name must be less than 50 characters';
	   END IF;
	   IF EXISTS (SELECT * FROM Menu WHERE itemName = NEW.itemName) THEN
	   RAISE EXCEPTION 'Another item with the same name exists!';
	   END IF;
	   
	   --Checks type constraints
	   IF NEW.type IS NULL THEN
           RAISE EXCEPTION 'Type cannot be null/empty';
           END IF;
           IF LENGTH(NEW.type) > 20 THEN
           RAISE EXCEPTION 'Type must be less than 50 characters';
           END IF;

	   --Checks price constraints
           IF NEW.price IS NULL THEN
           RAISE EXCEPTION 'Price cannot be null/empty';
           END IF;
	   IF NEW.price < 0 THEN
	   RAISE EXCEPTION 'Price must be non-negative';
	   END IF;

	   --Checks description constraints
           IF LENGTH(NEW.description) > 400 THEN
           RAISE EXCEPTION 'Description must be less than 400 characters';
           END IF;

	   --Checks imageURL constraints
           IF LENGTH(NEW.imageURL) > 256 THEN
           RAISE EXCEPTION 'Item name must be less than 256 characters';
           END IF;





	   RETURN NEW;
	END;
	$BODY$ 
LANGUAGE plpgsql VOLATILE;


DROP TRIGGER IF EXISTS menu_check_trigger ON Menu;
CREATE TRIGGER menu_check_trigger BEFORE INSERT 
ON Menu FOR EACH ROW 
EXECUTE PROCEDURE menu_check_procedure();


CREATE OR REPLACE FUNCTION users_check_procedure()
    RETURNS "trigger" AS
	$BODY$
        BEGIN
	   --Checks login constraints
	   IF NEW.login IS NULL THEN
		RAISE EXCEPTION 'Login cannot be null/empty';
           END IF;
	   IF LENGTH(NEW.login) > 50 THEN
		RAISE EXCEPTION 'Login must be less than 50 characters';
	   END IF;
	   IF EXISTS (SELECT * FROM Users WHERE login = NEW.login) THEN
	   RAISE EXCEPTION 'Another user with this login exists!';
	   END IF;
	   RETURN NEW;

	   --Checks phoneNum constraints
           IF LENGTH(NEW.phoneNum) > 16 THEN
                RAISE EXCEPTION 'Phone number must be less than 16 characters';
           END IF;
           IF EXISTS (SELECT * FROM Users WHERE phoneNum = NEW.phoneNum) THEN
           RAISE EXCEPTION 'Another user with this phone number exists!';
           END IF;

	   --Checks password constraints
           IF NEW.password IS NULL THEN
                RAISE EXCEPTION 'Password cannot be null/empty';
           END IF;
           IF LENGTH(NEW.password) > 50 THEN
                RAISE EXCEPTION 'Password must be less than 50 characters';
           END IF;

	   --Checks favItems constraints
           IF LENGTH(NEW.favItems) > 400 THEN
                RAISE EXCEPTION 'List of favorite items must be less than 50 characters';
           END IF;

	   --Checks type constraints
	   IF type != 'Manager' OR type != 'Employee' OR type != 'Customer' THEN
		RAISE EXCEPTION 'Type must be Manager, Employee, or Customer';
	   END IF;
           RETURN NEW;

       END;
       $BODY$
LANGUAGE plpgsql VOLATILE;


DROP TRIGGER IF EXISTS users_check_trigger ON Users;
CREATE TRIGGER users_check_trigger BEFORE INSERT
ON Users FOR EACH ROW
EXECUTE PROCEDURE users_check_procedure();                                           
