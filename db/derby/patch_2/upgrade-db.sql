-- IMPORTANT:
--
-- This file was generated by concatenating the three other .sql files together. It can be
-- used for testing, but the separate SQL sequences will be necessary if the Security Master
-- and Position Master need to be installed in different databases.
--
-- Please do not modify it - modify the originals and recreate this using 'ant create-db-sql'.


-- No upgrade operations required on COMMON
 

-- Security master upgrade from patch_1

ALTER TABLE sec_identifier_association ADD newValidStartDate TIMESTAMP;
ALTER TABLE sec_identifier_association ADD newValidEndDate TIMESTAMP;
UPDATE sec_identifier_association SET newValidStartDate=TIMESTAMP(validStartDate,'00:00'),newValidEndDate=TIMESTAMP(validEndDate,'00:00');
ALTER TABLE sec_identifier_association DROP validStartDate;
RENAME COLUMN sec_identifier_association.newValidStartDate TO validStartDate;
ALTER TABLE sec_identifier_association DROP validEndDate;
RENAME COLUMN sec_identifier_association.newValidEndDate TO validEndDate;

ALTER TABLE sec_equity ADD newEffectiveDateTime TIMESTAMP;
ALTER TABLE sec_equity ADD newLastModifiedDateTime TIMESTAMP;
UPDATE sec_equity SET newEffectiveDateTime=TIMESTAMP(effectiveDateTime,'00:00'),newLastModifiedDateTime=TIMESTAMP(lastModifiedDateTime,'00:00');
ALTER TABLE sec_equity ALTER COLUMN newEffectiveDateTime NOT NULL;
ALTER TABLE sec_equity ALTER COLUMN newLastModifiedDateTime NOT NULL; 
ALTER TABLE sec_equity DROP effectiveDateTime;
RENAME COLUMN sec_equity.newEffectiveDateTime TO effectiveDateTime;
ALTER TABLE sec_equity DROP lastModifiedDateTime;
RENAME COLUMN sec_equity.newLastModifiedDateTime TO lastModifiedDateTime;
ALTER TABLE sec_equity ADD shortName VARCHAR(255);

ALTER TABLE sec_option ADD newEffectiveDateTime TIMESTAMP;
ALTER TABLE sec_option ADD newLastModifiedDateTime TIMESTAMP;
ALTER TABLE sec_option ADD expiry_date TIMESTAMP;
UPDATE sec_option SET newEffectiveDateTime=TIMESTAMP(effectiveDateTime,'00:00'),newLastModifiedDateTime=TIMESTAMP(lastModifiedDateTime,'00:00'),expiry_date=TIMESTAMP(expiry,'00:00');
ALTER TABLE sec_option ALTER COLUMN newEffectiveDateTime NOT NULL;
ALTER TABLE sec_option ALTER COLUMN newLastModifiedDateTime NOT NULL;
ALTER TABLE sec_option ALTER COLUMN expiry_date NOT NULL;
ALTER TABLE sec_option DROP effectiveDateTime;
RENAME COLUMN sec_option.newEffectiveDateTime TO effectiveDateTime;
ALTER TABLE sec_option DROP lastModifiedDateTime;
RENAME COLUMN sec_option.newLastModifiedDateTime TO lastModifiedDateTime;
ALTER TABLE sec_option DROP expiry;
ALTER TABLE sec_option ADD expiry_accuracy SMALLINT NOT NULL DEFAULT 3;
ALTER TABLE sec_option ALTER COLUMN expiry_accuracy DROP DEFAULT;
ALTER TABLE sec_option ADD payment DOUBLE;
ALTER TABLE sec_option ADD lowerbound DOUBLE;
ALTER TABLE sec_option ADD upperbound DOUBLE;

ALTER TABLE sec_bond ADD newEffectiveDateTime TIMESTAMP;
ALTER TABLE sec_bond ADD newLastModifiedDateTime TIMESTAMP;
ALTER TABLE sec_bond ADD maturity_date TIMESTAMP;
ALTER TABLE sec_bond ADD announcement_date TIMESTAMP;
ALTER TABLE sec_bond ADD interestaccrual_date TIMESTAMP;
ALTER TABLE sec_bond ADD settlement_date TIMESTAMP;
ALTER TABLE sec_bond ADD firstcoupon_date TIMESTAMP;
UPDATE sec_bond SET
  newEffectiveDateTime=TIMESTAMP(effectiveDateTime,'00:00'),
  newLastModifiedDateTime=TIMESTAMP(lastModifiedDateTime,'00:00'),
  maturity_date=TIMESTAMP(maturity,'00:00'),
  announcement_date=TIMESTAMP(announcementdate,'00:00'),
  interestaccrual_date=TIMESTAMP(interestaccrualdate,'00:00'),
  settlement_date=TIMESTAMP(settlementdate,'00:00'),
  firstcoupon_date=TIMESTAMP(firstcoupondate,'00:00');
ALTER TABLE sec_bond ALTER COLUMN newEffectiveDateTime NOT NULL;
ALTER TABLE sec_bond ALTER COLUMN newLastModifiedDateTime NOT NULL;
ALTER TABLE sec_bond ALTER COLUMN maturity_date NOT NULL;
ALTER TABLE sec_bond ALTER COLUMN announcement_date NOT NULL;
ALTER TABLE sec_bond ALTER COLUMN interestaccrual_date NOT NULL;
ALTER TABLE sec_bond ALTER COLUMN settlement_date NOT NULL;
ALTER TABLE sec_bond ALTER COLUMN firstcoupon_date NOT NULL; 
ALTER TABLE sec_bond DROP effectiveDateTime;
RENAME COLUMN sec_bond.newEffectiveDateTime TO effectiveDateTime;
ALTER TABLE sec_bond DROP lastModifiedDateTime;
RENAME COLUMN sec_bond.newLastModifiedDateTime TO lastModifiedDateTime;
ALTER TABLE sec_bond DROP maturity;
ALTER TABLE sec_bond DROP announcementdate;
ALTER TABLE sec_bond DROP interestaccrualdate;
ALTER TABLE sec_bond DROP settlementdate;
ALTER TABLE sec_bond DROP firstcoupondate;
ALTER TABLE sec_bond ADD maturity_accuracy SMALLINT NOT NULL DEFAULT 3;
ALTER TABLE sec_bond ALTER COLUMN maturity_accuracy DROP DEFAULT;
ALTER TABLE sec_bond ADD announcement_zone VARCHAR(50) NOT NULL DEFAULT 'UTC';
ALTER TABLE sec_bond ALTER COLUMN announcement_zone DROP DEFAULT;
ALTER TABLE sec_bond ADD interestaccrual_zone VARCHAR(50) NOT NULL DEFAULT 'UTC';
ALTER TABLE sec_bond ALTER COLUMN interestaccrual_zone DROP DEFAULT;
ALTER TABLE sec_bond ADD settlement_zone VARCHAR(50) NOT NULL DEFAULT 'UTC';
ALTER TABLE sec_bond ALTER COLUMN settlement_zone DROP DEFAULT;
ALTER TABLE sec_bond ADD firstcoupon_zone VARCHAR(50) NOT NULL DEFAULT 'UTC';
ALTER TABLE sec_bond ALTER COLUMN firstcoupon_zone DROP DEFAULT; 

ALTER TABLE sec_future ADD newEffectiveDateTime TIMESTAMP;
ALTER TABLE sec_future ADD newLastModifiedDateTime TIMESTAMP;
ALTER TABLE sec_future ADD expiry_date TIMESTAMP;
UPDATE sec_future SET newEffectiveDateTime=TIMESTAMP(effectiveDateTime,'00:00'),newLastModifiedDateTime=TIMESTAMP(lastModifiedDateTime,'00:00'),expiry_date=TIMESTAMP(expiry,'00:00');
ALTER TABLE sec_future ALTER COLUMN newEffectiveDateTime NOT NULL;
ALTER TABLE sec_future ALTER COLUMN newLastModifiedDateTime NOT NULL;
ALTER TABLE sec_future ALTER COLUMN expiry_date NOT NULL;
ALTER TABLE sec_future DROP effectiveDateTime;
RENAME COLUMN sec_future.newEffectiveDateTime TO effectiveDateTime;
ALTER TABLE sec_future DROP lastModifiedDateTime;
RENAME COLUMN sec_future.newLastModifiedDateTime TO lastModifiedDateTime;
ALTER TABLE sec_future DROP expiry;
ALTER TABLE sec_future ADD expiry_accuracy SMALLINT NOT NULL DEFAULT 3;
ALTER TABLE sec_future ALTER COLUMN expiry_accuracy DROP DEFAULT;

ALTER TABLE sec_futurebundle ADD newStartDate TIMESTAMP;
ALTER TABLE sec_futurebundle ADD newEndDate TIMESTAMP;
UPDATE sec_futurebundle SET newStartDate=TIMESTAMP(startDate,'00:00'),newEndDate=TIMESTAMP(endDate,'00:00');
ALTER TABLE sec_futurebundle DROP startDate;
RENAME COLUMN sec_futurebundle.newStartDate TO startDate;
ALTER TABLE sec_futurebundle DROP endDate;
RENAME COLUMN sec_futurebundle.newEndDate TO endDate;

ALTER TABLE sec_cash ADD newEffectiveDateTime TIMESTAMP;
ALTER TABLE sec_cash ADD newLastModifiedDateTime TIMESTAMP;
UPDATE sec_cash SET newEffectiveDateTime=TIMESTAMP(effectiveDateTime,'00:00'),newLastModifiedDateTime=TIMESTAMP(lastModifiedDateTime,'00:00');
ALTER TABLE sec_cash ALTER COLUMN newEffectiveDateTime NOT NULL;
ALTER TABLE sec_cash ALTER COLUMN newLastModifiedDateTime NOT NULL;
ALTER TABLE sec_cash DROP effectiveDateTime;
RENAME COLUMN sec_cash.newEffectiveDateTime TO effectiveDateTime;
ALTER TABLE sec_cash DROP lastModifiedDateTime;
RENAME COLUMN sec_cash.newLastModifiedDateTime TO lastModifiedDateTime;

ALTER TABLE sec_fra ADD newEffectiveDateTime TIMESTAMP;
ALTER TABLE sec_fra ADD newLastModifiedDateTime TIMESTAMP;
ALTER TABLE sec_fra ADD start_date TIMESTAMP;
ALTER TABLE sec_fra ADD end_date TIMESTAMP;
UPDATE sec_fra SET
  newEffectiveDateTime=TIMESTAMP(effectiveDateTime,'00:00'),
  newLastModifiedDateTime=TIMESTAMP(lastModifiedDateTime,'00:00'),
  start_date=TIMESTAMP(startDate,'00:00'),
  end_date=TIMESTAMP(endDate,'00:00');
ALTER TABLE sec_fra ALTER COLUMN newEffectiveDateTime NOT NULL;
ALTER TABLE sec_fra ALTER COLUMN newLastModifiedDateTime NOT NULL;
ALTER TABLE sec_fra ALTER COLUMN start_date NOT NULL;
ALTER TABLE sec_fra ALTER COLUMN end_date NOT NULL;
ALTER TABLE sec_fra DROP effectiveDateTime;
RENAME COLUMN sec_fra.newEffectiveDateTime TO effectiveDateTime;
ALTER TABLE sec_fra DROP lastModifiedDateTime;
RENAME COLUMN sec_fra.newLastModifiedDateTime TO lastModifiedDateTime;
ALTER TABLE sec_fra DROP startdate;
ALTER TABLE sec_fra DROP enddate;
ALTER TABLE sec_fra ADD start_zone VARCHAR(50) NOT NULL DEFAULT 'UTC';
ALTER TABLE sec_fra ALTER COLUMN start_zone DROP DEFAULT;
ALTER TABLE sec_fra ADD end_zone VARCHAR(50) NOT NULL DEFAULT 'UTC';
ALTER TABLE sec_fra ALTER COLUMN end_zone DROP DEFAULT;

ALTER TABLE sec_swap ADD newEffectiveDateTime TIMESTAMP;
ALTER TABLE sec_swap ADD newLastModifiedDateTime TIMESTAMP;
ALTER TABLE sec_swap ADD trade_date TIMESTAMP;
ALTER TABLE sec_swap ADD effective_date TIMESTAMP;
ALTER TABLE sec_swap ADD maturity_date TIMESTAMP;
ALTER TABLE sec_swap ADD forwardstart_date TIMESTAMP;
UPDATE sec_swap SET
  newEffectiveDateTime=TIMESTAMP(effectiveDateTime,'00:00'),
  newLastModifiedDateTime=TIMESTAMP(lastModifiedDateTime,'00:00'),
  trade_date=TIMESTAMP(tradedate,'00:00'),
  effective_date=TIMESTAMP(effectivedate,'00:00'),
  maturity_date=TIMESTAMP(maturitydate,'00:00'),
  forwardstart_date=TIMESTAMP(forwardstartdate,'00:00');
ALTER TABLE sec_swap ALTER COLUMN newEffectiveDateTime NOT NULL;
ALTER TABLE sec_swap ALTER COLUMN newLastModifiedDateTime NOT NULL;
ALTER TABLE sec_swap ALTER COLUMN trade_date NOT NULL;
ALTER TABLE sec_swap ALTER COLUMN effective_date NOT NULL;
ALTER TABLE sec_swap ALTER COLUMN maturity_date NOT NULL;
ALTER TABLE sec_swap DROP effectiveDateTime;
RENAME COLUMN sec_swap.newEffectiveDateTime TO effectiveDateTime;
ALTER TABLE sec_swap DROP lastModifiedDateTime;
RENAME COLUMN sec_swap.newLastModifiedDateTime TO lastModifiedDateTime;
ALTER TABLE sec_swap DROP tradedate;
ALTER TABLE sec_swap DROP effectivedate;
ALTER TABLE sec_swap DROP maturitydate;
ALTER TABLE sec_swap DROP forwardstartdate;
ALTER TABLE sec_swap ADD trade_zone VARCHAR(50) NOT NULL DEFAULT 'UTC';
ALTER TABLE sec_swap ALTER COLUMN trade_zone DROP DEFAULT;
ALTER TABLE sec_swap ADD effective_zone VARCHAR(50) NOT NULL DEFAULT 'UTC';
ALTER TABLE sec_swap ALTER COLUMN effective_zone DROP DEFAULT;
ALTER TABLE sec_swap ADD maturity_zone VARCHAR(50) NOT NULL DEFAULT 'UTC';
ALTER TABLE sec_swap ALTER COLUMN maturity_zone DROP DEFAULT;
ALTER TABLE sec_swap ADD forwardstart_zone VARCHAR(50);
UPDATE sec_swap SET forwardstart_zone='UTC' WHERE forwardstart_date IS NOT NULL;
