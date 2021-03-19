FROM apache/nifi:1.13.0 as APP

WORKDIR /opt/nifi/

COPY nifi-custom-nar/target/nifi-custom-nar-*.nar ./nifi-current/lib/

ENTRYPOINT ["./scripts/start.sh"]