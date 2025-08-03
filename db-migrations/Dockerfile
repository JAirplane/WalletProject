FROM liquibase/liquibase:4.27

COPY ./db /liquibase/
COPY liquibase.properties /liquibase/
COPY entrypoint.sh /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]