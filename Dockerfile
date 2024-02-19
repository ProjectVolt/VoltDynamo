FROM debian:bookworm AS builder
ARG TARGETARCH
ARG TARGETVARIANT

ENV DEBIAN_FRONTEND=noninteractive
WORKDIR /app

RUN --mount=type=cache,target=/var/cache/apt,id=apt-cahce-1-$TARGETARCH$TARGETVARIANT-builder,sharing=locked \
    --mount=type=cache,target=/var/lib/apt,id=apt-cahce-2-$TARGETARCH$TARGETVARIANT-builder,sharing=locked \
    <<EOS
set -ex
rm -f /etc/apt/apt.conf.d/docker-clean
echo 'Binary::apt::APT::Keep-Downloaded-Packages "1";' > /etc/apt/apt.conf.d/keep-cache
echo 'APT::Install-Recommends "0";' > /etc/apt/apt.conf.d/no-recommends
echo 'APT::AutoRemove::RecommendsImportant "0";' >> /etc/apt/apt.conf.d/no-recommends
apt-get update
apt-get install -y libtool make cmake libseccomp-dev gcc python3 python3-venv
EOS

COPY judger/ /app/
RUN <<EOS
set -ex
mkdir /app/build
cmake -S . -B build
cmake --build build --parallel $(nproc)
EOS

FROM openjdk:22-bookworm
ARG TARGETARCH
ARG TARGETVARIANT

ENV DEBIAN_FRONTEND=noninteractive
WORKDIR /app

RUN --mount=type=cache,target=/var/cache/apt,id=apt-cahce-1-$TARGETARCH$TARGETVARIANT-final,sharing=locked \
    --mount=type=cache,target=/var/lib/apt,id=apt-cahce-2-$TARGETARCH$TARGETVARIANT-final,sharing=locked \
    <<EOS
set -ex
rm -f /etc/apt/apt.conf.d/docker-clean
echo 'Binary::apt::APT::Keep-Downloaded-Packages "1";' > /etc/apt/apt.conf.d/keep-cache
echo 'APT::Install-Recommends "0";' > /etc/apt/apt.conf.d/no-recommends
echo 'APT::AutoRemove::RecommendsImportant "0";' >> /etc/apt/apt.conf.d/no-recommends
needed="python3.11-minimal \
    python3.11-venv \
    libpython3.11-stdlib \
    libpython3.11-dev \
    gcc-12 \
    g++-12 \
    nodejs \
    maven \
    strace"
savedAptMark="$(apt-mark showmanual) $needed"
apt-get update
apt-get install -y ca-certificates curl gnupg
curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg
cat > /etc/apt/sources.list.d/nodesource.sources <<EOF
Types: deb
URIs: https://deb.nodesource.com/node_20.x
Suites: nodistro
Components: main
Signed-By:/etc/apt/keyrings/nodesource.gpg
EOF
apt-get update
apt-get install -y $needed
update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-12 12
update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-12 12
update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.11 11
apt-mark auto '.*' > /dev/null
apt-mark manual $savedAptMark
apt-get purge -y --auto-remove
EOS

COPY --from=builder --chmod=755 --link /app/output/libjudger.so /usr/lib/judger/libjudger.so

RUN <<EOS
set -ex
chmod -R u=rwX,go=rX /app/
useradd -u 901 -r -s /sbin/nologin -M compiler
useradd -u 902 -r -s /sbin/nologin -M code
useradd -u 903 -r -s /sbin/nologin -M -G code spj
EOS

WORKDIR /app

CMD [ "/bin/bash" ]