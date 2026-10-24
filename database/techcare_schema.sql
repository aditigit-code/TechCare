-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: techcare
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `customer_id` int unsigned NOT NULL AUTO_INCREMENT,
  `full_name` varchar(120) NOT NULL,
  `phone` varchar(25) NOT NULL,
  `email` varchar(150) DEFAULT NULL,
  `address` varchar(500) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`customer_id`),
  KEY `idx_customer_phone` (`phone`),
  KEY `idx_customer_name` (`full_name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES (1,'Demo Customer1','0000000000','demo@example.test','Fictional sample',1,'2026-10-23 20:54:29'),(2,'Aditi Dwivedi','1234567890',NULL,NULL,0,'2026-10-23 22:10:36');
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `device`
--

DROP TABLE IF EXISTS `device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `device` (
  `device_id` int unsigned NOT NULL AUTO_INCREMENT,
  `customer_id` int unsigned NOT NULL,
  `device_type` enum('LAPTOP','DESKTOP','ALL_IN_ONE','OTHER') NOT NULL,
  `brand` varchar(80) NOT NULL,
  `model` varchar(100) NOT NULL,
  `serial_number` varchar(100) DEFAULT NULL,
  `operating_system` varchar(100) DEFAULT NULL,
  `notes` varchar(500) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`device_id`),
  UNIQUE KEY `serial_number` (`serial_number`),
  KEY `idx_device_customer` (`customer_id`),
  CONSTRAINT `fk_device_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `device`
--

LOCK TABLES `device` WRITE;
/*!40000 ALTER TABLE `device` DISABLE KEYS */;
INSERT INTO `device` VALUES (1,1,'LAPTOP','Lenovo','ThinkPad E14','DEMO-TECHCARE-001','Windows 11',NULL,'2026-10-23 20:54:29'),(2,1,'LAPTOP','samsung','latest',NULL,NULL,NULL,'2026-10-23 23:39:18');
/*!40000 ALTER TABLE `device` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invoice`
--

DROP TABLE IF EXISTS `invoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invoice` (
  `invoice_id` int unsigned NOT NULL AUTO_INCREMENT,
  `ticket_id` int unsigned NOT NULL,
  `labor_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `parts_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `discount_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `tax_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `total_amount` decimal(10,2) NOT NULL,
  `issued_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`invoice_id`),
  UNIQUE KEY `ticket_id` (`ticket_id`),
  CONSTRAINT `fk_invoice_ticket` FOREIGN KEY (`ticket_id`) REFERENCES `service_ticket` (`ticket_id`) ON DELETE RESTRICT,
  CONSTRAINT `invoice_chk_1` CHECK ((`labor_amount` >= 0)),
  CONSTRAINT `invoice_chk_2` CHECK ((`parts_amount` >= 0)),
  CONSTRAINT `invoice_chk_3` CHECK ((`discount_amount` >= 0)),
  CONSTRAINT `invoice_chk_4` CHECK ((`tax_amount` >= 0)),
  CONSTRAINT `invoice_chk_5` CHECK ((`total_amount` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice`
--

LOCK TABLES `invoice` WRITE;
/*!40000 ALTER TABLE `invoice` DISABLE KEYS */;
INSERT INTO `invoice` VALUES (1,1,0.00,0.00,0.00,0.00,0.00,'2026-10-23 23:37:00'),(2,2,1000.00,3500.00,500.00,10.00,4010.00,'2026-10-23 23:43:51');
/*!40000 ALTER TABLE `invoice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `payment_id` int unsigned NOT NULL AUTO_INCREMENT,
  `invoice_id` int unsigned NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_method` enum('CASH','CARD','UPI','BANK_TRANSFER','OTHER') NOT NULL,
  `transaction_reference` varchar(100) DEFAULT NULL,
  `received_by` int unsigned NOT NULL,
  `paid_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `notes` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`payment_id`),
  KEY `fk_payment_staff` (`received_by`),
  KEY `idx_payment_invoice` (`invoice_id`),
  CONSTRAINT `fk_payment_invoice` FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`invoice_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_payment_staff` FOREIGN KEY (`received_by`) REFERENCES `staff_user` (`staff_user_id`) ON DELETE RESTRICT,
  CONSTRAINT `payment_chk_1` CHECK ((`amount` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment`
--

LOCK TABLES `payment` WRITE;
/*!40000 ALTER TABLE `payment` DISABLE KEYS */;
INSERT INTO `payment` VALUES (1,2,4010.00,'CASH',NULL,1,'2026-10-23 23:44:06',NULL);
/*!40000 ALTER TABLE `payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_category`
--

DROP TABLE IF EXISTS `service_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_category` (
  `category_id` int unsigned NOT NULL AUTO_INCREMENT,
  `category_name` varchar(80) NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `category_name` (`category_name`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_category`
--

LOCK TABLES `service_category` WRITE;
/*!40000 ALTER TABLE `service_category` DISABLE KEYS */;
INSERT INTO `service_category` VALUES (1,'Hardware',1),(2,'Software',1),(3,'Operating System',1),(4,'Networking',1),(5,'Malware Cleanup',1),(6,'Data Recovery',1),(7,'Maintenance',1);
/*!40000 ALTER TABLE `service_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_ticket`
--

DROP TABLE IF EXISTS `service_ticket`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_ticket` (
  `ticket_id` int unsigned NOT NULL AUTO_INCREMENT,
  `device_id` int unsigned NOT NULL,
  `category_id` int unsigned NOT NULL,
  `assigned_technician_id` int unsigned DEFAULT NULL,
  `created_by` int unsigned NOT NULL,
  `status` enum('RECEIVED','DIAGNOSING','AWAITING_APPROVAL','IN_PROGRESS','QUALITY_CHECK','READY_FOR_PICKUP','DELIVERED','ON_HOLD','CANCELLED') NOT NULL DEFAULT 'RECEIVED',
  `priority` enum('LOW','NORMAL','HIGH','URGENT') NOT NULL DEFAULT 'NORMAL',
  `reported_issue` text NOT NULL,
  `intake_condition` text,
  `accessories_received` varchar(500) DEFAULT NULL,
  `intake_notes` text,
  `diagnosis` text,
  `estimated_cost` decimal(10,2) DEFAULT NULL,
  `resolution` text,
  `expected_completion_date` date DEFAULT NULL,
  `received_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delivered_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`ticket_id`),
  KEY `fk_ticket_category` (`category_id`),
  KEY `fk_ticket_technician` (`assigned_technician_id`),
  KEY `fk_ticket_creator` (`created_by`),
  KEY `idx_ticket_device` (`device_id`),
  KEY `idx_ticket_status` (`status`),
  KEY `idx_ticket_received` (`received_at`),
  CONSTRAINT `fk_ticket_category` FOREIGN KEY (`category_id`) REFERENCES `service_category` (`category_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_ticket_creator` FOREIGN KEY (`created_by`) REFERENCES `staff_user` (`staff_user_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_ticket_device` FOREIGN KEY (`device_id`) REFERENCES `device` (`device_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_ticket_technician` FOREIGN KEY (`assigned_technician_id`) REFERENCES `technician_profile` (`technician_id`) ON DELETE RESTRICT,
  CONSTRAINT `service_ticket_chk_1` CHECK (((`estimated_cost` is null) or (`estimated_cost` >= 0)))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_ticket`
--

LOCK TABLES `service_ticket` WRITE;
/*!40000 ALTER TABLE `service_ticket` DISABLE KEYS */;
INSERT INTO `service_ticket` VALUES (1,1,2,1,1,'DELIVERED','URGENT','software installation',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-10-23 23:05:24','2026-10-24 00:00:01',NULL),(2,1,6,NULL,1,'RECEIVED','HIGH','data lost',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-10-23 23:41:26','2026-10-23 23:41:26',NULL);
/*!40000 ALTER TABLE `service_ticket` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `spare_part`
--

DROP TABLE IF EXISTS `spare_part`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `spare_part` (
  `part_id` int unsigned NOT NULL AUTO_INCREMENT,
  `sku` varchar(50) NOT NULL,
  `part_name` varchar(120) NOT NULL,
  `category` varchar(80) DEFAULT NULL,
  `quantity_in_stock` int unsigned NOT NULL DEFAULT '0',
  `reorder_level` int unsigned NOT NULL DEFAULT '0',
  `unit_cost` decimal(10,2) NOT NULL DEFAULT '0.00',
  `selling_price` decimal(10,2) NOT NULL DEFAULT '0.00',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`part_id`),
  UNIQUE KEY `sku` (`sku`),
  CONSTRAINT `spare_part_chk_1` CHECK ((`unit_cost` >= 0)),
  CONSTRAINT `spare_part_chk_2` CHECK ((`selling_price` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spare_part`
--

LOCK TABLES `spare_part` WRITE;
/*!40000 ALTER TABLE `spare_part` DISABLE KEYS */;
/*!40000 ALTER TABLE `spare_part` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `staff_user`
--

DROP TABLE IF EXISTS `staff_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `staff_user` (
  `staff_user_id` int unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(150) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('ADMIN','RECEPTIONIST','TECHNICIAN') NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`staff_user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `staff_user`
--

LOCK TABLES `staff_user` WRITE;
/*!40000 ALTER TABLE `staff_user` DISABLE KEYS */;
INSERT INTO `staff_user` VALUES (1,'Aditi',NULL,'pbkdf2$210000$9dRCc7LtBrLnAelNJnZlhQ$G-XERPbdki-j3KpzZgSpJ2VwnbptVPXaslLHVf1HO9c','ADMIN',1,'2026-10-23 22:44:33'),(2,'Ansh',NULL,'pbkdf2$210000$AulH9HX10twcnuExoJ6TVQ$efafJxRoG69KtAWIFj9vTEQZEUHsfT52cJSGoKy-FnY','TECHNICIAN',1,'2026-10-23 23:18:39');
/*!40000 ALTER TABLE `staff_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `technician_profile`
--

DROP TABLE IF EXISTS `technician_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `technician_profile` (
  `technician_id` int unsigned NOT NULL AUTO_INCREMENT,
  `staff_user_id` int unsigned NOT NULL,
  `specialization` varchar(100) DEFAULT NULL,
  `employment_status` enum('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`technician_id`),
  UNIQUE KEY `staff_user_id` (`staff_user_id`),
  CONSTRAINT `fk_technician_staff` FOREIGN KEY (`staff_user_id`) REFERENCES `staff_user` (`staff_user_id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `technician_profile`
--

LOCK TABLES `technician_profile` WRITE;
/*!40000 ALTER TABLE `technician_profile` DISABLE KEYS */;
INSERT INTO `technician_profile` VALUES (1,2,NULL,'ACTIVE');
/*!40000 ALTER TABLE `technician_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ticket_assignment_history`
--

DROP TABLE IF EXISTS `ticket_assignment_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ticket_assignment_history` (
  `assignment_id` int unsigned NOT NULL AUTO_INCREMENT,
  `ticket_id` int unsigned NOT NULL,
  `technician_id` int unsigned DEFAULT NULL,
  `assigned_by` int unsigned NOT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `assigned_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`assignment_id`),
  KEY `fk_assignment_ticket` (`ticket_id`),
  KEY `fk_assignment_technician` (`technician_id`),
  KEY `fk_assignment_staff` (`assigned_by`),
  CONSTRAINT `fk_assignment_staff` FOREIGN KEY (`assigned_by`) REFERENCES `staff_user` (`staff_user_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_assignment_technician` FOREIGN KEY (`technician_id`) REFERENCES `technician_profile` (`technician_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_assignment_ticket` FOREIGN KEY (`ticket_id`) REFERENCES `service_ticket` (`ticket_id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ticket_assignment_history`
--

LOCK TABLES `ticket_assignment_history` WRITE;
/*!40000 ALTER TABLE `ticket_assignment_history` DISABLE KEYS */;
INSERT INTO `ticket_assignment_history` VALUES (1,1,1,1,NULL,'2026-10-23 23:25:03');
/*!40000 ALTER TABLE `ticket_assignment_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ticket_part`
--

DROP TABLE IF EXISTS `ticket_part`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ticket_part` (
  `ticket_part_id` int unsigned NOT NULL AUTO_INCREMENT,
  `ticket_id` int unsigned NOT NULL,
  `part_id` int unsigned NOT NULL,
  `quantity` int unsigned NOT NULL,
  `unit_price_at_use` decimal(10,2) NOT NULL,
  `used_by` int unsigned NOT NULL,
  `used_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ticket_part_id`),
  UNIQUE KEY `uq_ticket_part` (`ticket_id`,`part_id`),
  KEY `fk_ticket_part_part` (`part_id`),
  KEY `fk_ticket_part_staff` (`used_by`),
  CONSTRAINT `fk_ticket_part_part` FOREIGN KEY (`part_id`) REFERENCES `spare_part` (`part_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_ticket_part_staff` FOREIGN KEY (`used_by`) REFERENCES `staff_user` (`staff_user_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_ticket_part_ticket` FOREIGN KEY (`ticket_id`) REFERENCES `service_ticket` (`ticket_id`) ON DELETE RESTRICT,
  CONSTRAINT `ticket_part_chk_1` CHECK ((`quantity` > 0)),
  CONSTRAINT `ticket_part_chk_2` CHECK ((`unit_price_at_use` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ticket_part`
--

LOCK TABLES `ticket_part` WRITE;
/*!40000 ALTER TABLE `ticket_part` DISABLE KEYS */;
/*!40000 ALTER TABLE `ticket_part` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ticket_status_history`
--

DROP TABLE IF EXISTS `ticket_status_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ticket_status_history` (
  `status_history_id` int unsigned NOT NULL AUTO_INCREMENT,
  `ticket_id` int unsigned NOT NULL,
  `old_status` enum('RECEIVED','DIAGNOSING','AWAITING_APPROVAL','IN_PROGRESS','QUALITY_CHECK','READY_FOR_PICKUP','DELIVERED','ON_HOLD','CANCELLED') DEFAULT NULL,
  `new_status` enum('RECEIVED','DIAGNOSING','AWAITING_APPROVAL','IN_PROGRESS','QUALITY_CHECK','READY_FOR_PICKUP','DELIVERED','ON_HOLD','CANCELLED') NOT NULL,
  `changed_by` int unsigned NOT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `changed_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`status_history_id`),
  KEY `fk_status_staff` (`changed_by`),
  KEY `idx_status_ticket_date` (`ticket_id`,`changed_at`),
  CONSTRAINT `fk_status_staff` FOREIGN KEY (`changed_by`) REFERENCES `staff_user` (`staff_user_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_status_ticket` FOREIGN KEY (`ticket_id`) REFERENCES `service_ticket` (`ticket_id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ticket_status_history`
--

LOCK TABLES `ticket_status_history` WRITE;
/*!40000 ALTER TABLE `ticket_status_history` DISABLE KEYS */;
INSERT INTO `ticket_status_history` VALUES (1,1,NULL,'RECEIVED',1,'Ticket created','2026-10-23 23:05:24'),(2,1,'RECEIVED','DIAGNOSING',1,NULL,'2026-10-23 23:24:07'),(3,2,NULL,'RECEIVED',1,'Ticket created','2026-10-23 23:41:26'),(4,1,'DIAGNOSING','DELIVERED',1,NULL,'2026-10-24 00:00:01');
/*!40000 ALTER TABLE `ticket_status_history` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-24 22:51:11
