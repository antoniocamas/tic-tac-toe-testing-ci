#! /usr/bin/env python
#
# coding: utf-8
"""
version updater script for Maven projects.
Reads pom.xml, parses the version, increments it and writes an updated pom to stdout.

Usage:
    pom-vbump.py [-i][-r] [-v <new version number>] [path to pom.xml]

Options:
    -r  readversion: just prints the version number without suffix
    -i  Edit pom.xml in place, instead of writing result to stdout
    -c  confirm current version. Removes suffix.
    -v  specify a version number, e.g. "1.23". It will add a suffix if not provided

If pom.xml file is not specified, the script will look in the current working directory.

Tested with Python 2.7.5 and 3.7.
Requires lxml.
"""
import sys
import getopt
import os.path
from lxml import etree as ET

SUFFIX = "-SNAPSHOT"

class InvalidVersion(Exception):
    def __init__(self, msg):
        self.msg = msg
    def __str__(self):
        return "Invalid version: " + self.msg


def main(args):
    version_to_write = None
    in_place = False
    readversion = False
    remove_suffix = False
    pom_xml = './pom.xml'

    try:
        opts, args = getopt.getopt(
            args, 'rv:ich', ['readversion', 'version',
                             'inplace', 'remove_suffix' ,'help'])
    except getopt.GetoptError:
        usage()
        return False

    for opt, value in opts:
        if opt in ('-h', '--help'):
            usage()
            return False
        elif opt in ('-v', '--version'):
            version_to_write = add_suffix_to_version(value)
        elif opt in ('-i', '--inplace'):
            in_place = True
        elif opt in ('-r', '--readversion'):
            readversion = True
        elif opt in ('-c', '--remove_suffix'):
            remove_suffix = True
        else:
            usage()
            return False

    if len(args) > 0:
        pom_xml = args[0]
    if not os.path.isfile(pom_xml):
        log("ERROR: Could not find pom.xml file: %s" %pom_xml)
        usage()
        return False

    try:
        bump(pom_xml, version_to_write, in_place, readversion, remove_suffix)
        return True
    except InvalidVersion as e:
        log(e)
        return False

def usage():
    print(__doc__)

def add_suffix_to_version(value):

    if not value.endswith(SUFFIX):
        return value + SUFFIX

def remove_suffix_from_version(value):

    if value.endswith(SUFFIX):
        return value.split(SUFFIX)[0]

    
def bump(pom_xml, version_to_write, in_place, readversion, remove_suffix):

    parser = ET.XMLParser(remove_comments=False)
    xml = ET.parse(pom_xml, parser=parser)

    # Find the project's current version
    version_tag = xml.find("./{*}version")
    if version_tag == None:
        raise InvalidVersion(
            "pom.xml does not appear to have a <version> tag")
    current_version = version_tag.text

    # If dry run, just print the next version and exit
    if readversion:
        print(remove_suffix_from_version(current_version))
        return

    # update version by removing suffix
    if remove_suffix:
        version_to_write = remove_suffix_from_version(current_version)
        
    # Update the XML
    version_tag.text = version_to_write
    
    if in_place:
        # Write back to pom.xml
        write_xml_to_file(xml, pom_xml)
    else:
        # Print result to stdout
        print_xml(xml)

def write_xml_to_file(xml, output_file):
    with open(output_file, 'wb') as f:
        f.write(ET.tostring(
            xml, encoding="utf-8", xml_declaration=True))

def print_xml(xml):
    result = ET.tostring(
        xml, encoding="utf-8", xml_declaration=True)
    if sys.hexversion >= 0x03000000:
        # Python 3.x
        sys.stdout.buffer.write(result)
    else:
        # Python 2.x
        print(result)

def log(msg):
    sys.stderr.write(str(msg) + '\n')

if __name__ == '__main__':
    sys.exit(not main(sys.argv[1:]))
