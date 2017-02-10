// vars/acme.groovy
def setName(value) {
    name = value
}
def getName() {
    name
}
def caution(message) {
    echo "Hello, ${name}! CAUTION: ${message}"
}
def run() {
    sh "echo ${name}"
}
