CREATE DATABASE IF NOT EXISTS maccs;
USE maccs;


CREATE TABLE `users` (
                         `id` char(36) NOT NULL,
                         `email` varchar(255) DEFAULT NULL,
                         `display_name` varchar(255) DEFAULT NULL,
                         `is_staff` tinyint(1) DEFAULT 0,
                         `password_hash` varchar(255) NOT NULL,
                         `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
                         `is_admin` tinyint(1) DEFAULT 0,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

LOCK TABLES `users` WRITE;
INSERT INTO `users` VALUES
                        ('00000000-0000-0000-0000-000000000001','demo@shop.local','Demo User',0,'noop','2025-09-18 07:37:39',0),
                        ('29bfd43f-e58f-4b27-abd3-58be3f6c4af1','tuandang24112004@gmail.com','tun123',0,'$2a$10$3R5YyVUYyf/RU/PEw8.AieRe0FR9JcH9pvpxmQ/koaehMnEP7Pcnq','2025-09-19 01:30:37',0),
                        ('3d5eb647-f22f-41cc-9559-b7d8345bc450','thaituan24112004@gmail.com','tuandang',0,'$2a$10$D9xKdbLi5hhRfNYlngTEd.SmuRnGZ1MX.Q7sfCVsi9okpAGrMIr0u','2025-09-19 01:25:00',0),
                        ('47ef7e89-4644-48a7-8d1b-5d071c479ddb','staff1@gmail.com','staff1',1,'$2a$10$7ZA4nRdyKFjxIn9zHODU2OVDdIzTpoYsLNRbz4SRDACurGJHMvVSy','2025-09-19 01:44:03',0),
                        ('c11ca4ef-c3f0-4562-8c33-ac3aa6f5d3be','admin@gmail.com','Admin',0,'$2a$10$vvl34CELMhtI2jLfJCOPce8iWL6EwATBNUIylTC8fAgUQ9p0zpgnW','2025-09-25 02:36:41',1);
UNLOCK TABLES;


CREATE TABLE `agents` (
                          `id` char(36) NOT NULL,
                          `handle` varchar(128) NOT NULL,
                          `display_name` varchar(255) NOT NULL,
                          `kind` varchar(64) NOT NULL,
                          `metadata` longtext DEFAULT NULL,
                          `active` tinyint(1) DEFAULT 1,
                          `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `handle` (`handle`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

LOCK TABLES `agents` WRITE;
INSERT INTO `agents` VALUES
                         ('1039dc66-fbb7-4000-a1c2-adbe0ad499b6','staff-agent','Staff Agent','StaffAgent',NULL,1,'2025-09-19 00:55:14'),
                         ('2057b76f-6bcd-4303-ab62-9ccc483bcb54','negative','Negative Agent','Negative',NULL,1,'2025-09-18 07:37:39'),
                         ('a889449c-cc83-49c5-bfb2-9349c303982c','neutral','Neutral Agent','Neutral',NULL,1,'2025-09-18 07:37:39'),
                         ('de69287e-b3bf-41ce-b69e-bb5103def39c','positive','Positive Agent','Positive',NULL,1,'2025-09-18 07:37:39'),
                         ('e9f20550-790d-431d-be8f-0de113bb8ec0','supervisor','Supervisor Agent','Supervisor',NULL,1,'2025-09-18 07:37:39');
UNLOCK TABLES;


CREATE TABLE `conversations` (
                                 `id` char(36) NOT NULL,
                                 `title` varchar(255) DEFAULT NULL,
                                 `created_by_user` char(36) DEFAULT NULL,
                                 `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
                                 `status` varchar(32) DEFAULT 'active',
                                 `is_staff_engaged` tinyint(1) DEFAULT 0,
                                 PRIMARY KEY (`id`),
                                 KEY `idx_conv_created_by` (`created_by_user`),
                                 CONSTRAINT `conversations_ibfk_1` FOREIGN KEY (`created_by_user`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `conversation_participants` (
                                             `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                             `conversation_id` char(36) NOT NULL,
                                             `participant_type` enum('user','agent') NOT NULL,
                                             `user_id` char(36) DEFAULT NULL,
                                             `agent_id` char(36) DEFAULT NULL,
                                             `role_key` enum('user','staff','agent','supervisor') NOT NULL,
                                             `joined_at` timestamp NOT NULL DEFAULT current_timestamp(),
                                             `left_at` timestamp NULL DEFAULT NULL,
                                             PRIMARY KEY (`id`),
                                             UNIQUE KEY `uniq_conv_user` (`conversation_id`,`user_id`),
                                             UNIQUE KEY `uniq_conv_agent` (`conversation_id`,`agent_id`),
                                             KEY `user_id` (`user_id`),
                                             KEY `agent_id` (`agent_id`),
                                             CONSTRAINT `conversation_participants_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`),
                                             CONSTRAINT `conversation_participants_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
                                             CONSTRAINT `conversation_participants_ibfk_3` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `messages` (
                            `id` char(36) NOT NULL,
                            `conversation_id` char(36) NOT NULL,
                            `sender_type` enum('user','agent','staff') NOT NULL,
                            `sender_user_id` char(36) DEFAULT NULL,
                            `sender_agent_id` char(36) DEFAULT NULL,
                            `role_key` varchar(32) NOT NULL,
                            `content` mediumtext NOT NULL,
                            `content_type` varchar(64) DEFAULT 'text/markdown',
                            `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
                            `edited_at` timestamp NULL DEFAULT NULL,
                            `deleted_at` timestamp NULL DEFAULT NULL,
                            `reply_to_message_id` char(36) DEFAULT NULL,
                            `meta` longtext DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            KEY `idx_msg_conv_created` (`conversation_id`,`created_at`),
                            KEY `reply_to_message_id` (`reply_to_message_id`),
                            CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`),
                            CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`reply_to_message_id`) REFERENCES `messages` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
