first note:

docker run -it  backend:1.0 1111 -Dakka.cluster.seed-nodes.0=akka.tcp://ClusterSystem@172.17.0.4:1111

following nodes:

docker run -it -p 8002:2552   frontend:1.0 -Dakka.cluster.seed-nodes.0=akka.tcp://ClusterSystem@10.0.2.15:8001 -e akka.remote.netty.tcp.hosname=10.0.2.15 akka.remote.netty.tcp.port=8002
