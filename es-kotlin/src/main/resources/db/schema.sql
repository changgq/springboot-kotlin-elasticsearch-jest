CREATE TABLE kes_clusters(
  id varchar(36)  not null,
  cluster_name  varchar(200) not null,
  cluster_url varchar(100) not null
);

ALTER TABLE kes_clusters  ADD
  CONSTRAINT PK_kes_clusters PRIMARY KEY
  (
    id
  );

commit;