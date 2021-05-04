#!/usr/bin/env python3
import os
import os.path
from lxml import etree
try: input = raw_input
except NameError: pass

ns = "http://maven.apache.org/POM/4.0.0"

def update_pom(v, pom):
    print(pom)
    dom = etree.parse(pom)
    root = dom.getroot()
    parent = root.find('{%s}parent' % ns)
    if parent:
        version = parent.find('{%s}version' % ns)
        if version is not None:
            version.text = v
    version = root.find('{%s}version' % ns)
    if version is not None:
        version.text = v
    with open(pom, 'wb') as fp:
        fp.write(b'<?xml version="1.0" encoding="UTF-8"?>\n')
        dom.write(fp)

def update_feature(v, feature):
    print(feature)
    dom = etree.parse(feature)
    root = dom.getroot()
    root.attrib['version'] = v
    with open(feature, 'wb') as fp:
        fp.write(b'<?xml version="1.0" encoding="UTF-8"?>\n')
        dom.write(fp)

def update_product(v, product):
    print(product)
    dom = etree.parse(product)
    root = dom.getroot()
    root.attrib['version'] = v
    features = root.find('features')
    if features:
        for f in features:
            f.attrib['version'] = v
    with open(product, 'wb') as fp:
        fp.write(b'<?xml version="1.0" encoding="UTF-8"?>\n')
        dom.write(fp)

def update_version(version):
    for bundle in os.listdir('.'):
        if 'com.ibh.systems' in bundle or 'org.eclipse.neoscada' in bundle:
            if (os.path.exists('./' + bundle + '/pom.xml')):
                update_pom(version, './' + bundle + '/pom.xml')
            if (os.path.exists('./' + bundle + '/feature.xml')):
                update_feature(version, './' + bundle + '/feature.xml')
            if (os.path.exists('./' + bundle + '/' + bundle) and bundle.endswith('.product')):
                update_product(version, './' + bundle + '/' + bundle)
    
version = input('version: ')
update_version(version.strip())
#update_version("0.22.0")